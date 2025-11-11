package org.aurifolia.cloud.id.client.provider;

import lombok.RequiredArgsConstructor;
import org.aurifolia.cloud.common.core.annotation.ConditionalOnPropertyPrefix;
import org.aurifolia.cloud.id.client.IdGeneratorProperties;
import org.aurifolia.cloud.id.common.entity.Segment;
import org.aurifolia.cloud.id.common.provider.SegmentProvider;
import org.aurifolia.cloud.id.metaserver.client.feign.IdMetaFeignClient;
import org.aurifolia.cloud.id.metaserver.common.dto.SegmentMetaDTO;
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
@ConditionalOnBean(IdMetaFeignClient.class)
@ConditionalOnPropertyPrefix("ginkgo.id.generator.segment")
@RequiredArgsConstructor
public class HttpSegmentProvider implements SegmentProvider {
    private final IdMetaFeignClient idMetaFeignClient;
    private final IdGeneratorProperties properties;

    @Override
    public Segment allocate() {
        SegmentMetaDTO segmentMetaDTO = idMetaFeignClient.nextSegment(properties.getSegment().getBizTag(), properties.getSegment().getStep());
        // 这里假设返回Map包含start、end字段
        return new Segment(segmentMetaDTO.getNextId(),
                segmentMetaDTO.getNextId() + segmentMetaDTO.getStep() - 1);
    }
} 