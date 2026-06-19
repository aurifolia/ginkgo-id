package org.aurifolia.cloud.id.sdk.internal;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

/**
 * 号段槽位
 * <p>
 * 双缓冲中的一个槽位，包含号段编号和原子序列号计数器
 *
 * @author Peng Dan
 * @since 2.0
 */
final class SegmentSlot {

    private static final VarHandle SEQUENCE;

    static {
        try {
            SEQUENCE = MethodHandles.lookup()
                    .findVarHandle(SegmentSlot.class, "sequence", long.class);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    final long segmentNumber;
    volatile long sequence = 0;

    SegmentSlot(long segmentNumber) {
        this.segmentNumber = segmentNumber;
    }

    static long getSequenceOpaque(SegmentSlot slot) {
        return (long) SEQUENCE.getOpaque(slot);
    }

    static boolean compareAndSetSequence(SegmentSlot slot, long expected, long update) {
        return SEQUENCE.compareAndSet(slot, expected, update);
    }

    static long getSequenceVolatile(SegmentSlot slot) {
        return (long) SEQUENCE.getVolatile(slot);
    }
}
