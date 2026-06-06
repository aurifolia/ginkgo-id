package org.aurifolia.cloud.id.infrastructure.segment.repository;

import lombok.RequiredArgsConstructor;
import org.aurifolia.cloud.id.domain.segment.entity.SegmentMeta;
import org.aurifolia.cloud.id.domain.segment.repository.SegmentMetaRepository;
import org.aurifolia.cloud.id.infrastructure.segment.mapper.SegmentMetaMapper;
import org.aurifolia.cloud.id.infrastructure.segment.po.SegmentMetaPO;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 号段元数据仓储实现
 *
 * @author Peng Dan
 * @since 2.0
 */
@Repository
@RequiredArgsConstructor
public class SegmentMetaRepositoryImpl implements SegmentMetaRepository {
    
    private final SegmentMetaMapper mapper;
    
    @Override
    public Optional<SegmentMeta> findByBizTag(String bizTag) {
        SegmentMetaPO po = mapper.selectByBizTag(bizTag);
        return Optional.ofNullable(po).map(this::convertToEntity);
    }
    
    @Override
    public void save(SegmentMeta meta) {
        SegmentMetaPO po = convertToPO(meta);
        mapper.insert(po);
        // 回填ID
        meta.setId(po.getId());
    }
    
    @Override
    public void update(SegmentMeta meta) {
        SegmentMetaPO po = convertToPO(meta);
        mapper.update(po);
    }
    
    /**
     * PO转Entity
     */
    private SegmentMeta convertToEntity(SegmentMetaPO po) {
        SegmentMeta entity = new SegmentMeta();
        entity.setId(po.getId());
        entity.setBizTag(po.getBizTag());
        entity.setNextId(po.getNextId());
        entity.setCreateTime(po.getCreateTime());
        entity.setUpdateTime(po.getUpdateTime());
        return entity;
    }
    
    /**
     * Entity转PO
     */
    private SegmentMetaPO convertToPO(SegmentMeta entity) {
        SegmentMetaPO po = new SegmentMetaPO();
        po.setId(entity.getId());
        po.setBizTag(entity.getBizTag());
        po.setNextId(entity.getNextId());
        po.setCreateTime(entity.getCreateTime());
        po.setUpdateTime(entity.getUpdateTime());
        return po;
    }
}
