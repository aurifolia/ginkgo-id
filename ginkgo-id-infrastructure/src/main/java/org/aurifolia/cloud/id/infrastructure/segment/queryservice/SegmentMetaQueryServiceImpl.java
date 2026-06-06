package org.aurifolia.cloud.id.infrastructure.segment.queryservice;

import lombok.RequiredArgsConstructor;
import org.aurifolia.cloud.id.application.segment.queryservice.SegmentMetaQueryService;
import org.aurifolia.cloud.id.domain.segment.entity.SegmentMeta;
import org.aurifolia.cloud.id.infrastructure.segment.mapper.SegmentMetaMapper;
import org.aurifolia.cloud.id.infrastructure.segment.po.SegmentMetaPO;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 号段元数据查询服务实现
 *
 * @author Peng Dan
 * @since 2.0
 */
@Component
@RequiredArgsConstructor
public class SegmentMetaQueryServiceImpl implements SegmentMetaQueryService {
    
    private final SegmentMetaMapper mapper;
    
    @Override
    public Optional<SegmentMeta> findByBizTagForUpdate(String bizTag) {
        SegmentMetaPO po = mapper.selectByBizTagForUpdate(bizTag);
        return Optional.ofNullable(po).map(this::convertToEntity);
    }
    
    private SegmentMeta convertToEntity(SegmentMetaPO po) {
        SegmentMeta entity = new SegmentMeta();
        entity.setId(po.getId());
        entity.setBizTag(po.getBizTag());
        entity.setNextId(po.getNextId());
        entity.setCreateTime(po.getCreateTime());
        entity.setUpdateTime(po.getUpdateTime());
        return entity;
    }
}
