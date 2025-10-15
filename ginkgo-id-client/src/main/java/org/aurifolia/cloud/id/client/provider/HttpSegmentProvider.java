package org.aurifolia.cloud.id.client.provider;

import org.aurifolia.cloud.common.core.annotation.ConditionalOnPropertyExists;
import org.aurifolia.cloud.id.client.IdGeneratorProperties;
import org.aurifolia.cloud.id.common.entity.Segment;
import org.aurifolia.cloud.id.common.provider.SegmentProvider;
import org.aurifolia.cloud.id.metaserver.client.feign.MetaFeignClient;
import org.aurifolia.cloud.id.metaserver.common.dto.SegmentMetaDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 通过http获取segment
 *
 * @author Peng Dan
 * @since 1.0
 */
@Component
@ConditionalOnPropertyExists("ginkgo.id.generator.segment")
public class HttpSegmentProvider implements SegmentProvider {
    @Autowired
    private MetaFeignClient metaFeignClient;
    @Autowired
    private IdGeneratorProperties properties;

    @Override
    public Segment allocate() {
        SegmentMetaDTO segmentMetaDTO = metaFeignClient.nextSegment(properties.getSegment().getBizTag(), properties.getSegment().getStep());
        // 这里假设返回Map包含start、end字段
        return new Segment(segmentMetaDTO.getNextId(),
                segmentMetaDTO.getNextId() + segmentMetaDTO.getStep() - 1);
    }
} 