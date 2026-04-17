package org.aurifolia.cloud.id.client.provider;

import lombok.RequiredArgsConstructor;
import org.aurifolia.cloud.common.core.annotation.ConditionalOnPropertyPrefix;
import org.aurifolia.cloud.id.api.dto.SegmentMetaDTO;
import org.aurifolia.cloud.id.api.entity.Segment;
import org.aurifolia.cloud.id.api.provider.SegmentProvider;
import org.aurifolia.cloud.id.api.service.IdMetaService;
import org.aurifolia.cloud.id.client.IdGeneratorProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * 通过http获取segment
 *
 * @author Peng Dan
 * @since 1.0
 */
@Primary
@Component
@RequiredArgsConstructor
@ConditionalOnBean(IdMetaService.class)
@ConditionalOnPropertyPrefix("ginkgo.id.generator.segment")
public class HttpSegmentProvider implements SegmentProvider {
    private final IdGeneratorProperties properties;
    private final IdMetaService idMetaService;

    @Override
    public Segment allocate() {
        SegmentMetaDTO segmentMetaDTO = idMetaService.nextSegment(properties.getSegment().getBizTag(), properties.getSegment().getStep());
        return new Segment(segmentMetaDTO.getNextId(), segmentMetaDTO.getNextId() + segmentMetaDTO.getStep() - 1);
    }
}
