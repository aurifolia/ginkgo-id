package org.aurifolia.cloud.id.sdk.internal;

/**
 * 线程本地ID分配器（TLAB）
 * <p>
 * 每个线程预分配一小段序列号，线程内不涉及并发操作。
 * 当预分配号段用完时，通过CAS从共享序列号中切割新块。
 * 块大小根据分配速率自适应调整（256~65536）。
 *
 * @author Peng Dan
 * @since 2.0
 */
final class ThreadLocalAllocator {

    private static final int INITIAL_CHUNK_SIZE = 4096;
    private static final int MIN_CHUNK_SIZE = 256;
    private static final int MAX_CHUNK_SIZE = 65536;
    private static final long FAST_THRESHOLD_NANOS = 10_000_000L;
    private static final long SLOW_THRESHOLD_NANOS = 1_000_000_000L;

    private final SegmentIdGenerator generator;

    private long segmentNumber = -1;
    private long seqStart = 0;
    private long seqEnd = 0;
    private long cursor = 0;
    private int chunkSize = INITIAL_CHUNK_SIZE;
    private long lastExhaustTimeNanos = 0;

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
        return IdFormat.composeNormal(segmentNumber, cursor++);
    }

    private boolean  allocateChunk() {
        long now = System.nanoTime();
        long[] chunk = generator.allocateChunk(chunkSize);
        if (chunk == null) {
            return false;
        }

        adaptChunkSize(now);

        this.segmentNumber = chunk[0];
        this.seqStart = chunk[1];
        this.seqEnd = chunk[2];
        this.cursor = seqStart;
        return true;
    }

    private void adaptChunkSize(long now) {
        if (lastExhaustTimeNanos != 0) {
            long elapsed = now - lastExhaustTimeNanos;
            if (elapsed < FAST_THRESHOLD_NANOS) {
                chunkSize = Math.min(chunkSize << 1, MAX_CHUNK_SIZE);
            } else if (elapsed > SLOW_THRESHOLD_NANOS) {
                chunkSize = Math.max(chunkSize >>> 1, MIN_CHUNK_SIZE);
            }
        }
        lastExhaustTimeNanos = now;
    }
}
