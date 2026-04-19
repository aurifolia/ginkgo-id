package org.aurifolia.cloud.id.client.generator;

import lombok.extern.slf4j.Slf4j;
import org.aurifolia.cloud.common.utils.ThreadUtil;
import org.aurifolia.cloud.id.api.entity.Segment;
import org.aurifolia.cloud.id.api.provider.SegmentProvider;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;

/**
 * 环形队列版分段ID生成器：
 * step为10000000，ringBuffer长度为8、200个消费者线程下，可以提供每秒4000W的id生成速度
 * 消费者线程数越高，tps越高，瓶颈在生产线程数
 *
 * @author Peng Dan
 * @since 1.0
 */
@Slf4j
public class SegmentEnhancedGenerator {
    private static final long INITIAL_PARK_NANOS = 1_000;
    private static final long MAX_PARK_NANOS = 100_000;
    private static final int DEFAULT_RING_SIZE = 4;
    private static final long SHUTDOWN_TIMEOUT_SECONDS = 5;
    
    private final SegmentProvider segmentProvider;
    private final SimpleSpmcRingBuffer<Segment> ringBuffer;
    private final ExecutorService executor;
    private final AtomicBoolean isShutdown = new AtomicBoolean(false);
    private final AtomicBoolean refillScheduled = new AtomicBoolean(false);

    public SegmentEnhancedGenerator(SegmentProvider segmentProvider) {
        this(segmentProvider, DEFAULT_RING_SIZE);
    }

    /**
     * 构造方法，指定环形队列大小
     */
    public SegmentEnhancedGenerator(SegmentProvider segmentProvider, int ringSize) {
        this(segmentProvider, ringSize, new ThreadPoolExecutor(
            1, // corePoolSize
            1, // maxPoolSize
            0L, TimeUnit.MILLISECONDS,
            new java.util.concurrent.ArrayBlockingQueue<>(10),
            r -> {
                Thread t = new Thread(r, "segment-loader");
                t.setDaemon(true);
                return t;
            },
            new ThreadPoolExecutor.DiscardPolicy()
        ));
    }

    /**
     * 构造方法，允许自定义线程池
     */
    public SegmentEnhancedGenerator(SegmentProvider segmentProvider, int ringSize, ExecutorService executor) {
        if (segmentProvider == null) throw new IllegalArgumentException("segmentProvider is null");
        if (executor == null) throw new IllegalArgumentException("executor is null");
        if (ringSize < 2) throw new IllegalArgumentException("ringSize must >= 2");
        // SimpleSpmcRingBuffer 内部已自动将容量调整为2的幂
        this.segmentProvider = segmentProvider;
        this.ringBuffer = new SimpleSpmcRingBuffer<>(ringSize);
        this.executor = executor;
        // 初始化填满环形队列（异步方式）
        triggerAsyncRefill();
    }

    /**
     * SPMC（单生产多消费）环形队列实现
     */
    static class SimpleSpmcRingBuffer<E> {
        private final E[] buffer;
        private final int mask;
        private final AtomicLong head = new AtomicLong(0);
        private volatile long tail = 0;
        @SuppressWarnings("unchecked")
        public SimpleSpmcRingBuffer(int capacity) {
            int cap = Integer.highestOneBit(capacity - 1) << 1;
            buffer = (E[]) new Object[cap];
            mask = cap - 1;
        }
        /**
         * 单生产者入队
         */
        public boolean offer(E e) {
            long t = tail;
            long h = head.get();
            if (t - h >= buffer.length) return false; // full
            buffer[(int)(t & mask)] = e;
            tail = t + 1;
            return true;
        }
        /**
         * 多消费者peek
         */
        public E peek() {
            long h = head.get();
            if (h >= tail) return null;
            return buffer[(int)(h & mask)];
        }
        /**
         * 多消费者poll
         */
        public E poll() {
            while (true) {
                long h = head.get();
                if (h >= tail) return null;
                if (head.compareAndSet(h, h + 1)) {
                    int idx = (int)(h & mask);
                    E e = buffer[idx];
                    buffer[idx] = null;
                    return e;
                }
            }
        }
        /**
         * 多消费者poll（只有slot内容等于expected时才移除，无锁并发保护）
         */
        public E pollIfMatch(E expected) {
            while (true) {
                long h = head.get();
                if (h >= tail) return null;
                int idx = (int)(h & mask);
                E e = buffer[idx];
                if (e != expected) return null;
                if (head.compareAndSet(h, h + 1)) {
                    buffer[idx] = null;
                    return e;
                }
            }
        }
        /**
         * 当前队列元素个数
         */
        public int size() {
            return (int)(tail - head.get());
        }
        /**
         * 队列容量
         */
        public int capacity() {
            return buffer.length;
        }
        /**
         * 仅供填充线程使用的原始访问
         */
        public E getRaw(int idx) {
            return buffer[idx & mask];
        }
        public void setRaw(int idx, E e) {
            buffer[idx & mask] = e;
        }
        public long getHead() {
            return head.get();
        }
    }

    /**
     * 获取下一个ID，极致高性能（用完即补齐策略，异步填充）
     */
    public long nextId() {
        long parkNanos = INITIAL_PARK_NANOS;
        
        while (!isShutdown.get()) {
            Segment segment = ringBuffer.peek();
            if (segment == null) {
                handleEmptyBuffer(parkNanos);
                // 指数退避：逐步增加等待时间
                parkNanos = Math.min(parkNanos << 1, MAX_PARK_NANOS);
                continue;
            }
            
            // 重置等待时间
            parkNanos = INITIAL_PARK_NANOS;
            
            long id = segment.nextId();
            if (id != -1) {
                return id;
            }
            
            // 段已用完，尝试移除并触发补充
            if (ringBuffer.pollIfMatch(segment) != null) {
                triggerAsyncRefill();
            }
            // 否则其他线程已处理，重试
        }
        throw new IllegalStateException("SegmentEnhancedGenerator is shutdown");
    }
    
    /**
     * 处理空缓冲区情况
     */
    private void handleEmptyBuffer(long parkNanos) {
        triggerAsyncRefill();
        LockSupport.parkNanos(parkNanos);
    }

    /**
     * 触发异步填充：仅当环未满且无正在进行的填充任务时提交，避免任务堆积
     */
    private void triggerAsyncRefill() {
        if (isShutdown.get() || ringBuffer.size() >= ringBuffer.capacity()) {
            return;
        }
        
        // 使用CAS确保同一时间只有一个填充任务在队列中
        if (refillScheduled.compareAndSet(false, true)) {
            executor.submit(() -> {
                try {
                    refillRingBuffer();
                } finally {
                    // 任务完成后重置标志，允许下次触发
                    refillScheduled.set(false);
                    // 如果完成后仍未满，再次触发（应对高并发消费场景）
                    if (!isShutdown.get() && ringBuffer.size() < ringBuffer.capacity()) {
                        triggerAsyncRefill();
                    }
                }
            });
        }
    }
    
    /**
     * 填充环形队列直到满或被关闭
     */
    private void refillRingBuffer() {
        while (!isShutdown.get() && ringBuffer.size() < ringBuffer.capacity()) {
            try {
                Segment allocated = segmentProvider.allocate();
                if (!ringBuffer.offer(allocated)) {
                    // 队列已满，退出
                    break;
                }
            } catch (Exception e) {
                log.error("Failed to allocate segment, will retry after 1 second", e);
                ThreadUtil.sleep(1, TimeUnit.SECONDS);
            }
        }
    }

    /**
     * 当前segment剩余可用ID数
     */
    public long currentSegmentRemaining() {
        Segment seg = ringBuffer.peek();
        return seg != null ? seg.remaining() : 0;
    }

    /**
     * 关闭预加载线程池，等待正在执行的任务完成
     */
    public void shutdown() {
        if (isShutdown.compareAndSet(false, true)) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                    log.warn("Force shutdown executor due to timeout");
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
                log.warn("Shutdown interrupted");
            }
        }
    }
}
