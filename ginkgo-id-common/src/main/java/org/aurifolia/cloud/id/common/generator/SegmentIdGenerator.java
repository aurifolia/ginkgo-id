package org.aurifolia.cloud.id.common.generator;

import lombok.extern.slf4j.Slf4j;
import org.aurifolia.cloud.common.utils.ThreadUtil;
import org.aurifolia.cloud.id.common.entity.Segment;
import org.aurifolia.cloud.id.common.provider.SegmentProvider;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
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
public class SegmentIdGenerator {
    private static final long PARTIAL_WAIT_NANOS = 100_000;
    private static final int DEFAULT_RING_SIZE = 4;
    private final SegmentProvider segmentProvider;
    private final SimpleSpmcRingBuffer<Segment> ringBuffer;
    private final ExecutorService executor;

    public SegmentIdGenerator(SegmentProvider segmentProvider) {
        this(segmentProvider, DEFAULT_RING_SIZE);
    }

    /**
     * 构造方法，指定环形队列大小
     */
    public SegmentIdGenerator(SegmentProvider segmentProvider, int ringSize) {
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
    public SegmentIdGenerator(SegmentProvider segmentProvider, int ringSize, ExecutorService executor) {
        if (segmentProvider == null) throw new IllegalArgumentException("segmentProvider is null");
        if (executor == null) throw new IllegalArgumentException("executor is null");
        if (ringSize < 2) throw new IllegalArgumentException("ringSize must >= 2");
        // SimpleSpmcRingBuffer 内部已自动将容量调整为2的幂
        this.segmentProvider = segmentProvider;
        this.ringBuffer = new SimpleSpmcRingBuffer<>(ringSize);
        this.executor = executor;
        // 初始化填满环形队列（异步方式）
        asyncRefillAllEmptySegmentsFromNext();
    }

    /**
     * SPMC（单生产多消费）环形队列实现
     */
    static class SimpleSpmcRingBuffer<E> {
        private final E[] buffer;
        private final int mask;
        private final java.util.concurrent.atomic.AtomicInteger head = new java.util.concurrent.atomic.AtomicInteger(0);
        private volatile int tail = 0;
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
            int t = tail;
            int h = head.get();
            if (t - h >= buffer.length) return false; // full
            buffer[t & mask] = e;
            tail = t + 1;
            return true;
        }
        /**
         * 多消费者peek
         */
        public E peek() {
            int h = head.get();
            if (h >= tail) return null;
            return buffer[h & mask];
        }
        /**
         * 多消费者poll
         */
        public E poll() {
            while (true) {
                int h = head.get();
                if (h >= tail) return null;
                if (head.compareAndSet(h, h + 1)) {
                    int idx = h & mask;
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
                int h = head.get();
                if (h >= tail) return null;
                int idx = h & mask;
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
            return tail - head.get();
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
        public int getHead() {
            return head.get();
        }
    }

    /**
     * 获取下一个ID，极致高性能（用完即补齐策略，异步填充）
     */
    public long nextId() {
        while (true) {
            Segment segment = ringBuffer.peek();
            if (segment == null) {
                asyncRefillAllEmptySegmentsFromNext();
                // 段未准备好，等待并重试
                LockSupport.parkNanos(PARTIAL_WAIT_NANOS);
                continue;
            }
            long id = segment.nextId();
            if (id != -1) {
                return id;
            } else {
                // 无锁并发保护：只有peek到的segment还在slot时才移除
                if (ringBuffer.pollIfMatch(segment) != null) {
                    asyncRefillAllEmptySegmentsFromNext();
                }
                // 否则重试
            }
        }
    }

    /**
     * 用完即补齐：异步补满整个环形队列所有空槽或已用完槽，从队尾补充，直到填满
     */
    private void asyncRefillAllEmptySegmentsFromNext() {
        executor.submit(() -> {
            while (ringBuffer.size() < ringBuffer.capacity()) {
                try {
                    Segment allocated = segmentProvider.allocate();
                    ringBuffer.offer(allocated);
                } catch (Exception e) {
                    log.error("allocate segment failed", e);
                    ThreadUtil.sleep(1, TimeUnit.SECONDS);
                }
            }
        });
    }

    /**
     * 当前segment剩余可用ID数
     */
    public long currentSegmentRemaining() {
        Segment seg = ringBuffer.peek();
        return seg != null ? seg.remaining() : 0;
    }

    /**
     * 关闭预加载线程池
     */
    public void shutdown() {
        executor.shutdown();
    }
} 