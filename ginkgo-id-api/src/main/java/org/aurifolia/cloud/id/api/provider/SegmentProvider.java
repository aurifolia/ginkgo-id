package org.aurifolia.cloud.id.api.provider;

import org.aurifolia.cloud.id.api.entity.Segment;

/**
 * ID段提供器
 *
 * @author Peng Dan
 * @since 1.0
 */
@FunctionalInterface
public interface SegmentProvider {
    /**
     * 分配ID段
     *
     * @return ID段
     */
    Segment allocate();
}
