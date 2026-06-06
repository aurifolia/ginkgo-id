package org.aurifolia.cloud.id.sdk.internal;

import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 降级模式ID生成器
 * <p>
 * 不依赖任何外部服务，使用IP生成machineId，逻辑时钟永不回退。
 * 同一秒序列号耗尽时直接推进到下一秒。
 * ID格式: [0|0|clock(32bit,秒)|machine(10bit)|seq(20bit)]
 *
 * @author Peng Dan
 * @since 2.0
 */
public final class DegradedIdGenerator {

    private final long machineId;
    private final AtomicLong clockAndSeq = new AtomicLong(0);

    public DegradedIdGenerator() {
        this.machineId = resolveMachineId();
    }

    /**
     * 生成降级模式ID
     *
     * @return 降级模式ID
     */
    public long nextId() {
        while (true) {
            long current = clockAndSeq.get();
            long clock = current >>> IdFormat.SEQ_BITS;
            long seq = current & IdFormat.MAX_SEQ;

            long nowSeconds = System.currentTimeMillis() / 1000;
            long newClock = Math.max(nowSeconds, clock);
            long newSeq = (newClock == clock) ? seq + 1 : 0;

            if (newSeq > IdFormat.MAX_SEQ) {
                newClock = clock + 1;
                newSeq = 0;
            }

            long newValue = (newClock << IdFormat.SEQ_BITS) | newSeq;
            if (clockAndSeq.compareAndSet(current, newValue)) {
                return IdFormat.composeDegraded(newClock, machineId, newSeq);
            }
        }
    }

    private static long resolveMachineId() {
        try {
            byte[] addr = InetAddress.getLocalHost().getAddress();
            int hash = ((addr[addr.length - 2] & 0xFF) << 8) | (addr[addr.length - 1] & 0xFF);
            return hash & IdFormat.DEGRADED_MAX_MACHINE;
        } catch (Exception e) {
            return (long) (Math.random() * (IdFormat.DEGRADED_MAX_MACHINE + 1));
        }
    }
}
