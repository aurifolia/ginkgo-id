package org.aurifolia.cloud.id.client.provider;

import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboReference;
import org.aurifolia.cloud.common.core.annotation.ConditionalOnPropertyPrefix;
import org.aurifolia.cloud.id.api.dto.SegmentMetaDTO;
import org.aurifolia.cloud.id.api.entity.Segment;
import org.aurifolia.cloud.id.api.provider.SegmentProvider;
import org.aurifolia.cloud.id.api.service.IdMetaService;
import org.aurifolia.cloud.id.client.IdGeneratorProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

/**
 * 基于Dubbo的segment提供者
 *
 * @author Peng Dan
 * @since 1.0
 */
@Component
@RequiredArgsConstructor
@ConditionalOnClass(DubboReference.class)
@ConditionalOnPropertyPrefix("ginkgo.id.generator.segment")
public class DubboSegmentProvider implements SegmentProvider {
    private final IdGeneratorProperties properties;
    @DubboReference
    private IdMetaService idMetaService;

    @Override
    public Segment allocate() {
        SegmentMetaDTO segmentMetaDTO = idMetaService.nextSegment(properties.getSegment().getBizTag(), properties.getSegment().getStep());
        // 这里假设返回Map包含start、end字段
        return new Segment(segmentMetaDTO.getNextId(),
                segmentMetaDTO.getNextId() + segmentMetaDTO.getStep() - 1);
    }
}
