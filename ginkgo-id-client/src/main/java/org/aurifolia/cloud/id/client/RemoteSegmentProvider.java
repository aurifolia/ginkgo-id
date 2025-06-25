package org.aurifolia.cloud.id.client;

import org.aurifolia.cloud.id.common.entity.Segment;
import org.aurifolia.cloud.id.common.provider.SegmentProvider;
import org.aurifolia.cloud.id.metaserver.client.MetaFeignClient;
import org.aurifolia.cloud.id.client.IdGeneratorProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RemoteSegmentProvider implements SegmentProvider {
    @Autowired
    private MetaFeignClient metaFeignClient;
    @Autowired
    private IdGeneratorProperties properties;

    @Override
    public Segment allocate() {
        String bizTag = properties.getBizTag();
        Map<String, Object> resp = metaFeignClient.nextSegment(bizTag);
        // 这里假设返回Map包含start、end字段
        Long start = ((Number) resp.getOrDefault("start", 0L)).longValue();
        Long end = ((Number) resp.getOrDefault("end", 0L)).longValue();
        return new Segment(start, end);
    }
} 