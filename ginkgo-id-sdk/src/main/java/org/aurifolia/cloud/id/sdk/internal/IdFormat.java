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

    static final int DEGRADED_MACHINE_BITS = 10;
    static final long DEGRADED_MAX_MACHINE = (1L << DEGRADED_MACHINE_BITS) - 1;

    /**
     * 组装正常模式ID
     */
    static long composeNormal(long segmentNumber, long sequence) {
        return (1L << 62) | ((segmentNumber & 0x3FFFFFFFFFFL) << SEQ_BITS) | (sequence & MAX_SEQ);
    }

    /**
     * 组装降级模式ID
     */
    static long composeDegraded(long clock, long machineId, long sequence) {
        return ((clock & 0xFFFFFFFFL) << 30)
                | ((machineId & DEGRADED_MAX_MACHINE) << SEQ_BITS)
                | (sequence & MAX_SEQ);
    }
}
