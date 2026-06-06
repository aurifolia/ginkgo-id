package org.aurifolia.cloud.id.sdk.internal;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 号段双缓冲
 * <p>
 * 维护两个号段槽位，当一个使用完时切换到另一个，同时异步补充新的号段。
 * 使用CAS状态机（NORMAL -> SWITCHING -> NORMAL）保证只有一个线程执行切换。
 *
 * @author Peng Dan
 * @since 2.0
 */
final class SegmentBuffer {

    static final int STATE_NORMAL = 0;
    static final int STATE_SWITCHING = 1;
    static final int STATE_DEGRADED = 2;

    volatile SegmentSlot[] slots = new SegmentSlot[2];
    volatile int activeSlot = 0;
    final AtomicInteger state = new AtomicInteger(STATE_NORMAL);

    /**
     * 初始化双缓冲
     */
    void init(long seg0, long seg1) {
        slots[0] = new SegmentSlot(seg0);
        slots[1] = new SegmentSlot(seg1);
        activeSlot = 0;
        state.set(STATE_NORMAL);
    }

    /**
     * 补充非活跃槽位
     */
    void refillInactiveSlot(long segmentNumber) {
        int inactive = 1 - activeSlot;
        slots[inactive] = new SegmentSlot(segmentNumber);
    }

    /**
     * 尝试切换到另一个槽位
     *
     * @return 切换成功返回true，另一个槽位也已耗尽返回false
     */
    boolean trySwitch() {
        int current = activeSlot;
        int next = 1 - current;
        if (slots[next].sequence.get() < IdFormat.MAX_SEQ) {
            activeSlot = next;
            return true;
        }
        return false;
    }
}
