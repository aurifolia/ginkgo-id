package org.aurifolia.cloud.id.common.generator;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * 基于snowflake的id生成器
 * ringBuffer为1024*1024、fillBatchSize为10000，200个消费者线程下，可以提供每秒2000W+的id生成速度
 * 单个消费线程下，可以提供每秒1.4亿的id生成速度
 * 消费者线程数越多，tps越低，瓶颈在消费者线程数
 *
 * @author Peng Dan
 * @since 1.0
 */
public class SnowflakeEnhancedGenerator {
    /**
     * snowflake生成器
     */
    private static class BatchSnowflakeGenerator {
        private static final int TIMESTAMP_BITS = 30;
        private static final int MACHINE_ID_BITS = 8;
        private static final int SEQUENCE_BITS = 25;
        private static final long MAX_SEQUENCE = (1L << SEQUENCE_BITS) - 1;
        private static final int MACHINE_ID_SHIFT = SEQUENCE_BITS;
        private static final int TIMESTAMP_SHIFT = SEQUENCE_BITS + MACHINE_ID_BITS;
        private static final int TIME_UNIT_MILLIS_SHIFT = 14;
        private static final long EPOCH_MILLIS = 1750694400000L;
        private final long machineId;
        private long lastTimestamp = 0;
        private long sequence = 0;

        public BatchSnowflakeGenerator(long machineId) {
            this.machineId = machineId;
        }

        public int generateBatch(long[] output, int count) {
            long currentTimestamp = getCurrentTimestamp();
            if (currentTimestamp < lastTimestamp) {
                currentTimestamp = lastTimestamp;
            } else if (currentTimestamp > lastTimestamp) {
                sequence = 0;
            }
            lastTimestamp = currentTimestamp;
            for (int i = 0; i < count; i++) {
                if (sequence > MAX_SEQUENCE) {
                    currentTimestamp++;
                    sequence = 0;
                    lastTimestamp = currentTimestamp;
                }
                output[i] = (currentTimestamp << TIMESTAMP_SHIFT) |
                        (machineId << MACHINE_ID_SHIFT) |
                        sequence++;
            }
            return count;
        }

        private long getCurrentTimestamp() {
            long currentTimeMillis = System.currentTimeMillis();
            long elapsedMillis = currentTimeMillis - EPOCH_MILLIS;
            return elapsedMillis >> TIME_UNIT_MILLIS_SHIFT;
        }
    }

    /**
     * 高性能大容量循环队列
     */
    private static class WatermarkTriggeredRingBuffer {
        private final long[] buffer;
        private final int mask;
        public final int capacity;
        private final int fillTriggerLevel;

        // Consumer fields
        private volatile long readIndex = 0;
        // 伪共享填充
        private long p1, p2, p3, p4, p5, p6, p7;

        // Producer fields
        private volatile long writeIndex = 0;
        // 伪共享填充
        private long p11, p12, p13, p14, p15, p16, p17;

        private volatile boolean fillInProgress = false;
        private volatile boolean needsFilling = true;
        private Thread producerThread = null;

        public WatermarkTriggeredRingBuffer(int capacity) {
            int actualCapacity = Integer.highestOneBit(capacity - 1) << 1;
            this.buffer = new long[actualCapacity];
            this.mask = actualCapacity - 1;
            this.capacity = actualCapacity;
            this.fillTriggerLevel = capacity * 3 / 4;
        }

        public int batchOffer(long[] values, int count) {
            long currentWrite = writeIndex;
            long available = readIndex + capacity - currentWrite;
            if (available < count) {
                count = (int) Math.max(0, available);
            }
            if (count == 0) {
                return 0;
            }
            int bufferIndex = (int) (currentWrite & mask);
            if (bufferIndex + count <= capacity) {
                System.arraycopy(values, 0, buffer, bufferIndex, count);
            } else {
                int part1 = capacity - bufferIndex;
                System.arraycopy(values, 0, buffer, bufferIndex, part1);
                System.arraycopy(values, part1, buffer, 0, count - part1);
            }
            writeIndex = currentWrite + count;
            return count;
        }

        public boolean offer(long value) {
            long currentWrite = writeIndex;
            long nextWrite = currentWrite + 1;
            if (nextWrite - readIndex > capacity) {
                return false;
            }
            buffer[(int) (currentWrite & mask)] = value;
            writeIndex = nextWrite;
            return true;
        }

        public long poll() {
            long currentRead = readIndex;
            if (currentRead >= writeIndex) {
                checkWatermarkAndTriggerFill();
                return -1;
            }
            long value = buffer[(int) (currentRead & mask)];
            readIndex = currentRead + 1;
            checkWatermarkAndTriggerFill();
            return value;
        }

        public int batchPoll(long[] output, int maxCount) {
            long currentRead = readIndex;
            long available = writeIndex - currentRead;
            int toRead = (int) Math.min(maxCount, available);
            if (toRead > 0) {
                int bufferIndex = (int) (currentRead & mask);
                if (bufferIndex + toRead <= capacity) {
                    System.arraycopy(buffer, bufferIndex, output, 0, toRead);
                } else {
                    int part1 = capacity - bufferIndex;
                    System.arraycopy(buffer, bufferIndex, output, 0, part1);
                    System.arraycopy(buffer, 0, output, part1, toRead - part1);
                }
                readIndex = currentRead + toRead;
            }
            checkWatermarkAndTriggerFill();
            return toRead;
        }

        private void checkWatermarkAndTriggerFill() {
            int currentSize = size();
            if (currentSize <= fillTriggerLevel && !needsFilling && !fillInProgress) {
                needsFilling = true;
                if (producerThread != null) {
                    LockSupport.unpark(producerThread);
                }
            }
        }

        public int size() {
            return (int) (writeIndex - readIndex);
        }

        public boolean needsFilling() {
            return needsFilling;
        }

        public void setProducerThread(Thread producerThread) {
            this.producerThread = producerThread;
        }

        public void markFillStart() {
            fillInProgress = true;
            needsFilling = false;
        }

        public void markFillComplete() {
            fillInProgress = false;
        }
    }

    /**
     * ID生产者
     */
    private static class WatermarkTriggeredProducer extends Thread {
        private final WatermarkTriggeredRingBuffer buffer;
        private final BatchSnowflakeGenerator generator;
        private final int fillBatchSize;
        private final long initialIdleNanos;
        private final long maxIdleNanos;
        private volatile boolean running = true;

        public WatermarkTriggeredProducer(WatermarkTriggeredRingBuffer buffer,
                                          BatchSnowflakeGenerator generator,
                                          int fillBatchSize, long maxIdleTime) {
            this.buffer = buffer;
            this.generator = generator;
            this.fillBatchSize = fillBatchSize;
            this.maxIdleNanos = TimeUnit.MILLISECONDS.toNanos(maxIdleTime);
            this.initialIdleNanos = Math.min(maxIdleNanos, TimeUnit.MILLISECONDS.toNanos(10));
            this.setDaemon(true);
            this.setName("snowflake-producer");
            this.setPriority(Thread.NORM_PRIORITY - 1);
        }

        @Override
        public void run() {
            long[] batchBuffer = new long[fillBatchSize];
            while (running) {
                try {
                    if (buffer.needsFilling()) {
                        performFill(batchBuffer);
                    } else {
                        performIdleWait();
                    }
                } catch (Exception e) {
                    System.err.println("Producer error: " + e.getMessage());
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        private void performFill(long[] batchBuffer) {
            buffer.markFillStart();
            try {
                while (running) {
                    int size = buffer.size();
                    int available = buffer.capacity - size;
                    if (available <= 0) break;
                    int generated = generator.generateBatch(batchBuffer, Math.min(available, fillBatchSize));
                    buffer.batchOffer(batchBuffer, generated);
                }
            } finally {
                buffer.markFillComplete();
            }
        }

        private void performIdleWait() throws InterruptedException {
            long waitTimeNanos = initialIdleNanos;
            while (!buffer.needsFilling() && running) {
                LockSupport.parkNanos(waitTimeNanos);
                waitTimeNanos = Math.min(waitTimeNanos << 1, maxIdleNanos);
            }
        }

        public void shutdown() {
            this.running = false;
        }
    }

    /**
     * 高性能消费者
     */
    private record HighPerformanceConsumer(WatermarkTriggeredRingBuffer buffer) {
        private static final int MAX_SPIN = 1000;
        private static final int PARK_NANOS = 10_000;

        public long getId() {
            return buffer.poll();
        }

        public int getIds(long[] output, int maxCount) {
            return buffer.batchPoll(output, maxCount);
        }

        public long getIdBlocking() throws InterruptedException {
            long id;
            int spin = 0;
            while ((id = buffer.poll()) == -1) {
                if (spin < MAX_SPIN) {
                    Thread.onSpinWait();
                    spin++;
                } else {
                    LockSupport.parkNanos(PARK_NANOS);
                }
                if (Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException();
                }
            }
            return id;
        }
    }

    /**
     * ID缓存
     */
    public static class IdCache {
        private final WatermarkTriggeredProducer producer;
        private final HighPerformanceConsumer consumer;

        public IdCache(long machineId, int bufferSize, int fillBatchSize, long maxIdleTime) {
            WatermarkTriggeredRingBuffer buffer = new WatermarkTriggeredRingBuffer(bufferSize);
            BatchSnowflakeGenerator generator = new BatchSnowflakeGenerator(machineId);
            this.producer = new WatermarkTriggeredProducer(buffer, generator, fillBatchSize, maxIdleTime);
            this.consumer = new HighPerformanceConsumer(buffer);
            buffer.setProducerThread(this.producer);
        }

        public void start() {
            producer.start();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        public void shutdown() {
            producer.shutdown();
            try {
                producer.join(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        public long getId() {
            return consumer.getId();
        }

        public long getIdBlocking() throws InterruptedException {
            return consumer.getIdBlocking();
        }

        public int getIds(long[] output, int maxCount) {
            return consumer.getIds(output, maxCount);
        }
    }
}