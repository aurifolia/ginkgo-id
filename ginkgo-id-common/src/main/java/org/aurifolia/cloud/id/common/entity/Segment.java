package org.aurifolia.cloud.id.common.entity;

import java.util.concurrent.atomic.AtomicLong;

/**
 * ID段信息
 *
 * @author Peng Dan
 * @since 1.0
 */
public class Segment {
    private final long start;
    private final long end;
    private final AtomicLong current;

    public Segment(long start, long end) {
        if (end < start) throw new IllegalArgumentException("end < start");
        this.start = start;
        this.end = end;
        this.current = new AtomicLong(start);
    }

    /** 返回下一个ID，如果用完返回-1 */
    public long nextId() {
        long id = current.getAndIncrement();
        if (id > end) return -1;
        return id;
    }

    public long size() {
        return end - start + 1;
    }

    public long used() {
        return current.get() - start;
    }

    public long remaining() {
        return end - current.get() + 1;
    }

    public double usageRatio() {
        return (double) used() / size();
    }
}
