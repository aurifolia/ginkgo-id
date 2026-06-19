package org.aurifolia.cloud.id.sdk.internal;

/**
 * ID位格式工具
 * <p>
 * 正常模式: [符号位(1bit,0) | 标识位(1bit,1) | 号段编号(42bit) | 序列号(20bit)]
 * 降级模式: [符号位(1bit,0) | 标识位(1bit,0) | 逻辑时钟(32bit,秒) | 机器标识(10bit) | 序列号(20bit)]
 *
 * @author Peng Dan
 * @since 2.0
 */
final class IdFormat {

    static final int SEQ_BITS = 20;
    static final long MAX_SEQ = (1L << SEQ_BITS) - 1;

    static final int SEGMENT_BITS = 42;
    static final long SEGMENT_MASK = (1L << SEGMENT_BITS) - 1;
    static final long NORMAL_FLAG = 1L << (SEQ_BITS + SEGMENT_BITS);

    static final int DEGRADED_MACHINE_BITS = 10;
    static final long DEGRADED_MAX_MACHINE = (1L << DEGRADED_MACHINE_BITS) - 1;

    static final int DEGRADED_CLOCK_BITS = 32;
    static final long DEGRADED_CLOCK_MASK = (1L << DEGRADED_CLOCK_BITS) - 1;
    static final int DEGRADED_FLAG_SHIFT = SEQ_BITS + DEGRADED_MACHINE_BITS;

    /**
     * 组装正常模式ID
     */
    static long composeNormal(long segmentNumber, long sequence) {
        return NORMAL_FLAG | ((segmentNumber & SEGMENT_MASK) << SEQ_BITS) | (sequence & MAX_SEQ);
    }

    /**
     * 组装降级模式ID
     */
    static long composeDegraded(long clock, long machineId, long sequence) {
        return ((clock & DEGRADED_CLOCK_MASK) << DEGRADED_FLAG_SHIFT)
                | ((machineId & DEGRADED_MAX_MACHINE) << SEQ_BITS)
                | (sequence & MAX_SEQ);
    }
}
