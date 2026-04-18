package org.aurifolia.cloud.id.client.generator;

import org.aurifolia.cloud.id.api.IdGenerator;
import org.aurifolia.cloud.id.api.provider.SegmentProvider;
import org.springframework.beans.factory.DisposableBean;

/**
 * 基于段的ID生成器
 *
 * @author Peng Dan
 * @since 1.0
 */
public class SegmentIdGenerator implements IdGenerator, DisposableBean {
    private final SegmentEnhancedGenerator segmentGenerator;

    public SegmentIdGenerator(SegmentProvider provider) {
        this.segmentGenerator = new SegmentEnhancedGenerator(provider);
    }

    public SegmentIdGenerator(SegmentProvider provider, int ringSize) {
        this.segmentGenerator = new SegmentEnhancedGenerator(provider, ringSize);
    }

    @Override
    public long nextId() {
        return segmentGenerator.nextId();
    }

    @Override
    public void destroy() {
        segmentGenerator.shutdown();
    }
}
