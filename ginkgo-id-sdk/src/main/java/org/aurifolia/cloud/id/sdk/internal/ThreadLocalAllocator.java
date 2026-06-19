package org.aurifolia.cloud.id.sdk.internal;

/**
 * 线程本地ID分配器（TLAB）
 * <p>
 * 每个线程预分配一小段序列号，线程内不涉及并发操作。
 * 当预分配号段用完时，通过CAS从共享序列号中切割新块。
 * 块大小基于EMA（指数移动平均）平滑估算分配速率，按比例自适应调整（256~65536）。
 *
 * @author Peng Dan
 * @since 2.0
 */
final class ThreadLocalAllocator {

    private static final int INITIAL_CHUNK_SIZE = 4096;
    private static final int MIN_CHUNK_SIZE = 256;
    private static final int MAX_CHUNK_SIZE = 65536;
    private static final long TARGET_INTERVAL_NANOS = 100_000_000L;
    private static final long EMA_WEIGHT = 4;
    private static final long EMA_DIVISOR = EMA_WEIGHT + 1;

    private final SegmentIdGenerator generator;
    private final Chunk chunk = new Chunk();

    private long segmentPrefix;
    private long seqEnd;
    private long cursor;
    private int chunkSize = INITIAL_CHUNK_SIZE;
    private long lastAllocTimeNanos = 0;
    private long emaIntervalNanos = -1;

    ThreadLocalAllocator(SegmentIdGenerator generator) {
        this.generator = generator;
    }

    /**
     * 从线程本地缓冲区获取下一个ID
     *
     * @return ID值，如果号段已耗尽且无法分配新块则返回-1
     */
    long nextId() {
        if (cursor >= seqEnd) {
            if (!allocateChunk()) {
                return -1;
            }
        }
        return segmentPrefix | cursor++;
    }

    private boolean allocateChunk() {
        if (!generator.allocateChunk(chunkSize, chunk)) {
            return false;
        }

        adaptChunkSize(System.nanoTime());

        this.segmentPrefix = IdFormat.NORMAL_FLAG | ((chunk.segmentNumber & IdFormat.SEGMENT_MASK) << IdFormat.SEQ_BITS);
        this.seqEnd = chunk.seqEnd;
        this.cursor = chunk.seqStart;
        return true;
    }

    private void adaptChunkSize(long now) {
        if (lastAllocTimeNanos == 0) {
            lastAllocTimeNanos = now;
            return;
        }
        long intervalNanos = now - lastAllocTimeNanos;
        lastAllocTimeNanos = now;

        emaIntervalNanos = emaIntervalNanos < 0 ? intervalNanos
                : (intervalNanos + emaIntervalNanos * EMA_WEIGHT) / EMA_DIVISOR;

        if (emaIntervalNanos < TARGET_INTERVAL_NANOS >> 1) {
            chunkSize = Math.min(chunkSize << 1, MAX_CHUNK_SIZE);
        } else if (emaIntervalNanos > TARGET_INTERVAL_NANOS << 1) {
            chunkSize = Math.max(chunkSize >> 1, MIN_CHUNK_SIZE);
        }
    }
}
