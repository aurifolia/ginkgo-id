package org.aurifolia.cloud.id.common.provider;

import org.aurifolia.cloud.id.common.entity.Segment;

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
