package org.aurifolia.cloud.id.client;

import org.aurifolia.cloud.id.common.generator.SegmentIdGenerator;
import org.aurifolia.cloud.id.common.provider.SegmentProvider;

/**
 * 基于段的ID生成器
 *
 * @author Peng Dan
 * @since 1.0
 */
public class SegmentIdGeneratorImpl implements IdGenerator {
    private final SegmentIdGenerator segmentIdGenerator;

    public SegmentIdGeneratorImpl(SegmentProvider provider) {
        this.segmentIdGenerator = new SegmentIdGenerator(provider);
    }

    public SegmentIdGeneratorImpl(SegmentProvider provider, int ringSize) {
        this.segmentIdGenerator = new SegmentIdGenerator(provider, ringSize);
    }

    @Override
    public long nextId() {
        return segmentIdGenerator.nextId();
    }
} 