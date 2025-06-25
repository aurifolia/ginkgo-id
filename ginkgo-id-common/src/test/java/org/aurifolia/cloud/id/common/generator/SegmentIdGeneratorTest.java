package org.aurifolia.cloud.id.common.generator;

import org.aurifolia.cloud.id.common.entity.Segment;
import org.aurifolia.cloud.id.common.provider.SegmentProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Map;

class SegmentIdGeneratorTest {
    static class MockSegmentProvider implements SegmentProvider {
        private long base = 0;
        @Override
        public Segment allocate() {
            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            long start = base;
            base += 10000000L;
            return new Segment(start, start + 9999999L);
        }
    }

    static class HttpSegmentProvider implements SegmentProvider {
        private final String bizTag = "test12345";
        private final String url = "http://localhost:8080/api/segment/next";

        @Override
        public Segment allocate() {
            try {
                java.net.URL u = new java.net.URL(url + "?bizTag=" + bizTag + "&step=10000000");
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) u.openConnection();
                conn.setRequestMethod("POST");
                conn.setConnectTimeout(3000);
                conn.setReadTimeout(3000);
                conn.setDoOutput(true);
                conn.connect();
                int code = conn.getResponseCode();
                if (code != 200) throw new RuntimeException("HTTP error: " + code);
                java.io.InputStream is = conn.getInputStream();
                StringBuilder sb = new StringBuilder();
                try (java.util.Scanner scanner = new java.util.Scanner(is, "UTF-8")) {
                    while (scanner.hasNextLine()) sb.append(scanner.nextLine());
                }
                String json = sb.toString();
                // 简单解析json，假设返回{"nextId":123,"step":1000,...}
                long nextId = extractLong(json, "nextId");
                long step = extractLong(json, "step");
                return new Segment(nextId, nextId + step - 1);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        private long extractLong(String json, String key) {
            String pattern = "\\\"" + key + "\\\":(\\d+)";
            java.util.regex.Matcher m = java.util.regex.Pattern.compile(pattern).matcher(json);
            if (m.find()) return Long.parseLong(m.group(1));
            throw new RuntimeException("Key not found: " + key);
        }
    }

    @Test
    void testBasicIdGeneration() {
        SegmentIdGenerator generator = new SegmentIdGenerator(new MockSegmentProvider(), 4);
        long prev = generator.nextId();
        for (int i = 0; i < 5000; i++) {
            long id = generator.nextId();
            Assertions.assertTrue(id > prev, "ID should be increasing");
            prev = id;
        }
        generator.shutdown();
    }

    @Test
    void testConcurrentIdGeneration() throws InterruptedException {
        SegmentIdGenerator generator = new SegmentIdGenerator(new MockSegmentProvider(), 8);
        int threadCount = 8;
        int idsPerThread = 2000;
        Set<Long> allIds = java.util.Collections.synchronizedSet(new HashSet<>());
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        for (int t = 0; t < threadCount; t++) {
            pool.submit(() -> {
                for (int i = 0; i < idsPerThread; i++) {
                    allIds.add(generator.nextId());
                }
                latch.countDown();
            });
        }
        latch.await();
        pool.shutdown();
        generator.shutdown();
        Assertions.assertEquals(threadCount * idsPerThread, allIds.size(), "All IDs should be unique");
    }

    @Test
    void testMonitorPerformanceAndResource() throws InterruptedException, Exception {
        SegmentIdGenerator generator = new SegmentIdGenerator(new MockSegmentProvider(), 4);
        TimeUnit.SECONDS.sleep(2);
        int threadCount = 8;
        int testSeconds = 10;
        java.util.concurrent.atomic.AtomicLong idCounter = new java.util.concurrent.atomic.AtomicLong();
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // 反射获取ringBuffer、head、tail
        java.lang.reflect.Field ringBufferField = generator.getClass().getDeclaredField("ringBuffer");
        ringBufferField.setAccessible(true);
        Object ringBuffer = ringBufferField.get(generator);
        java.lang.reflect.Field headField = ringBuffer.getClass().getDeclaredField("head");
        headField.setAccessible(true);
        java.lang.reflect.Field tailField = ringBuffer.getClass().getDeclaredField("tail");
        tailField.setAccessible(true);
        java.lang.reflect.Method sizeMethod = ringBuffer.getClass().getDeclaredMethod("size");
        sizeMethod.setAccessible(true);

        // 监控相关
        ScheduledExecutorService monitor = Executors.newSingleThreadScheduledExecutor();
        long[] lastCount = {0};
        long startTime = System.currentTimeMillis();
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        Runtime runtime = Runtime.getRuntime();
        long endTime = startTime + testSeconds * 1000L;
        monitor.scheduleAtFixedRate(() -> {
            long currentCount = idCounter.get();
            long tps = currentCount - lastCount[0];
            lastCount[0] = currentCount;
            long usedMem = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
            double cpuLoad = 0;
            try {
                cpuLoad = (osBean instanceof com.sun.management.OperatingSystemMXBean)
                        ? ((com.sun.management.OperatingSystemMXBean) osBean).getProcessCpuLoad() * 100 : -1;
            } catch (Exception ignored) {}
            long segRemain = generator.currentSegmentRemaining();
            int segCount = 0;
            int head = 0, tail = 0;
            try {
                segCount = (int) sizeMethod.invoke(ringBuffer);
                Object headObj = headField.get(ringBuffer);
                if (headObj instanceof java.util.concurrent.atomic.AtomicInteger) {
                    head = ((java.util.concurrent.atomic.AtomicInteger) headObj).get();
                }
                tail = (int) tailField.get(ringBuffer);
            } catch (Exception e) {}
            System.out.printf("[Monitor] TPS: %d, Total: %d, CPU: %.2f%%, UsedMem: %dMB, SegmentRemain: %d, SegCount: %d, Head: %d, Tail: %d\n",
                    tps, currentCount, cpuLoad, usedMem, segRemain, segCount, head, tail);
        }, 1, 2, TimeUnit.SECONDS);
        AtomicLong counter = new AtomicLong();
        for (int t = 0; t < threadCount; t++) {
            pool.submit(() -> {
                while (System.currentTimeMillis() < endTime) {
                    generator.nextId();
                    idCounter.incrementAndGet();
                }
                latch.countDown();
            });
        }
        latch.await();
        pool.shutdown();
        generator.shutdown();
        monitor.shutdownNow();
        long duration = System.currentTimeMillis() - startTime;
        System.out.println("==== Test Finished ====");
        System.out.printf("Total IDs: %d, Duration: %.2fs, Avg TPS: %.2f\n",
                idCounter.get(), duration / 1000.0, idCounter.get() * 1000.0 / duration);
        long usedMem = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
        System.out.printf("Final Used Memory: %dMB\n", usedMem);
    }

    @Test
    void testHttpSegmentProviderPerformance() throws Exception {
        SegmentProvider provider = new HttpSegmentProvider();
        SegmentIdGenerator generator = new SegmentIdGenerator(provider, 8);
        TimeUnit.SECONDS.sleep(2);
        int threadCount = 200;
        int testSeconds = 1000;
        java.util.concurrent.atomic.AtomicLong idCounter = new java.util.concurrent.atomic.AtomicLong();
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        ScheduledExecutorService monitor = Executors.newSingleThreadScheduledExecutor();
        long[] lastCount = {0};
        long startTime = System.currentTimeMillis();
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        Runtime runtime = Runtime.getRuntime();
        long endTime = startTime + testSeconds * 1000L;

        // 反射获取ringBuffer、head、tail
        java.lang.reflect.Field ringBufferField = generator.getClass().getDeclaredField("ringBuffer");
        ringBufferField.setAccessible(true);
        Object ringBuffer = ringBufferField.get(generator);
        java.lang.reflect.Field headField = ringBuffer.getClass().getDeclaredField("head");
        headField.setAccessible(true);
        java.lang.reflect.Field tailField = ringBuffer.getClass().getDeclaredField("tail");
        tailField.setAccessible(true);
        java.lang.reflect.Method sizeMethod = ringBuffer.getClass().getDeclaredMethod("size");
        sizeMethod.setAccessible(true);

        monitor.scheduleAtFixedRate(() -> {
            long currentCount = idCounter.get();
            long tps = currentCount - lastCount[0];
            lastCount[0] = currentCount;
            long usedMem = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
            double cpuLoad = 0;
            try {
                cpuLoad = (osBean instanceof com.sun.management.OperatingSystemMXBean)
                        ? ((com.sun.management.OperatingSystemMXBean) osBean).getProcessCpuLoad() * 100 : -1;
            } catch (Exception ignored) {}
            long segRemain = generator.currentSegmentRemaining();
            int segCount = 0;
            int head = 0, tail = 0;
            try {
                segCount = (int) sizeMethod.invoke(ringBuffer);
                Object headObj = headField.get(ringBuffer);
                if (headObj instanceof java.util.concurrent.atomic.AtomicInteger) {
                    head = ((java.util.concurrent.atomic.AtomicInteger) headObj).get();
                }
                tail = (int) tailField.get(ringBuffer);
            } catch (Exception e) {}
            System.out.printf("[Monitor] TPS: %d, Total: %d, CPU: %.2f%%, UsedMem: %dMB, SegmentRemain: %d, SegCount: %d, Head: %d, Tail: %d\n",
                    tps, currentCount, cpuLoad, usedMem, segRemain, segCount, head, tail);
        }, 1, 2, TimeUnit.SECONDS);
        for (int t = 0; t < threadCount; t++) {
            pool.submit(() -> {
                while (System.currentTimeMillis() < endTime) {
                    generator.nextId();
                    idCounter.incrementAndGet();
                }
                latch.countDown();
            });
        }
        latch.await();
        pool.shutdown();
        generator.shutdown();
        monitor.shutdownNow();
        long duration = System.currentTimeMillis() - startTime;
        System.out.println("==== HTTP SegmentProvider Test Finished ====");
        System.out.printf("Total IDs: %d, Duration: %.2fs, Avg TPS: %.2f\n",
                idCounter.get(), duration / 1000.0, idCounter.get() * 1000.0 / duration);
        long usedMem = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
        System.out.printf("Final Used Memory: %dMB\n", usedMem);
    }
} 