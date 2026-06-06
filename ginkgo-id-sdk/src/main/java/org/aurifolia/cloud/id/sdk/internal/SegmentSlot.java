package org.aurifolia.cloud.id.sdk.internal;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 号段槽位
 * <p>
 * 双缓冲中的一个槽位，包含号段编号和原子序列号计数器
 *
 * @author Peng Dan
 * @since 2.0
 */
final class SegmentSlot {

    final long segmentNumber;
    final AtomicLong sequence = new AtomicLong(0);

    SegmentSlot(long segmentNumber) {
        this.segmentNumber = segmentNumber;
    }
}
