package org.aurifolia.cloud.id.client;

import org.aurifolia.cloud.id.common.generator.SegmentIdGenerator;
import org.aurifolia.cloud.id.common.provider.SegmentProvider;

public class SegmentIdGeneratorImpl implements IdGenerator {
    private final SegmentIdGenerator segmentIdGenerator;

    public SegmentIdGeneratorImpl(SegmentProvider provider) {
        this.segmentIdGenerator = new SegmentIdGenerator(provider);
    }

    @Override
    public long nextId() {
        return segmentIdGenerator.nextId();
    }
} 