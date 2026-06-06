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
        return Optional.ofNullable(po).map(this::toEntity);
    }

    @Override
    public Optional<SegmentMeta> findByBizTagForUpdate(String bizTag) {
        SegmentMetaPO po = mapper.selectByBizTagForUpdate(bizTag);
        return Optional.ofNullable(po).map(this::toEntity);
    }

    @Override
    public void save(SegmentMeta meta) {
        SegmentMetaPO po = toPO(meta);
        mapper.insert(po);
        meta.setId(po.getId());
    }

    @Override
    public void update(SegmentMeta meta) {
        SegmentMetaPO po = toPO(meta);
        mapper.update(po);
    }

    private SegmentMeta toEntity(SegmentMetaPO po) {
        return SegmentMeta.reconstitute(
                po.getId(), po.getBizTag(), po.getNextSegmentNumber(),
                po.getCreateTime(), po.getUpdateTime());
    }

    private SegmentMetaPO toPO(SegmentMeta entity) {
        SegmentMetaPO po = new SegmentMetaPO();
        po.setId(entity.getId());
        po.setBizTag(entity.getBizTag());
        po.setNextSegmentNumber(entity.getNextSegmentNumber());
        po.setCreateTime(entity.getCreateTime());
        po.setUpdateTime(entity.getUpdateTime());
        return po;
    }
}
