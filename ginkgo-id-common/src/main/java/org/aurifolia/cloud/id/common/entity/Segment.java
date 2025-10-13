package org.aurifolia.cloud.id.common.entity;

import java.util.concurrent.atomic.AtomicLong;

/**
 * ID段信息，[start, end]之间的ID都能获取
 *
 * @author Peng Dan
 * @since 1.0
 */
public class Segment {
    private final long start;
    private final long end;
    private final AtomicLong current;

    /**
     * 创建一个ID段
     *
     * @param start 开始ID，包含
     * @param end   结束ID，包含
     */
    public Segment(long start, long end) {
        if (end < start) throw new IllegalArgumentException("end < start");
        this.start = start;
        this.end = end;
        this.current = new AtomicLong(start);
    }

    /**
     * 返回下一个ID，如果用完返回-1
     *
     * @return 下一个ID
     */
    public long nextId() {
        long id = current.getAndIncrement();
        if (id > end) return -1;
        return id;
    }

    /**
     * id段大小
     *
     * @return id段大小
     */
    public long size() {
        return end - start + 1;
    }

    /**
     * 已使用的ID数量
     *
     * @return 已使用的ID数量
     */
    public long used() {
        return current.get() - start;
    }

    /**
     * 剩余的ID数量
     *
     * @return 剩余的ID数量
     */
    public long remaining() {
        return end - current.get() + 1;
    }

    /**
     * id段的使用率
     *
     * @return 使用率
     */
    public double usageRatio() {
        return (double) used() / size();
    }
}
