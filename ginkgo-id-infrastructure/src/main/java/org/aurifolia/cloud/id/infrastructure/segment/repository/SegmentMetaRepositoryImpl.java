package org.aurifolia.cloud.id.infrastructure.segment.repository;

import lombok.RequiredArgsConstructor;
import org.aurifolia.cloud.id.domain.segment.entity.SegmentMeta;
import org.aurifolia.cloud.id.domain.segment.repository.SegmentMetaRepository;
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

    private final SegmentMetaJpaRepository jpaRepository;

    @Override
    public Optional<SegmentMeta> findByBizTag(String bizTag) {
        return jpaRepository.findByBizTag(bizTag).map(this::toEntity);
    }

    @Override
    public Optional<SegmentMeta> findByBizTagForUpdate(String bizTag) {
        return jpaRepository.findWithLockByBizTag(bizTag).map(this::toEntity);
    }

    @Override
    public void save(SegmentMeta meta) {
        SegmentMetaPO po = toPO(meta);
        SegmentMetaPO saved = jpaRepository.save(po);
        meta.assignId(saved.getId());
    }

    @Override
    public void update(SegmentMeta meta) {
        SegmentMetaPO po = toPO(meta);
        jpaRepository.save(po);
    }

    private SegmentMeta toEntity(SegmentMetaPO po) {
        return SegmentMeta.reconstitute(
                po.getId(), po.getBizTag(), po.getMaxId(),
                po.getCreateTime(), po.getUpdateTime());
    }

    private SegmentMetaPO toPO(SegmentMeta entity) {
        SegmentMetaPO po = new SegmentMetaPO();
        po.setId(entity.getId());
        po.setBizTag(entity.getBizTag());
        po.setMaxId(entity.getMaxId());
        po.setCreateTime(entity.getCreateTime());
        po.setUpdateTime(entity.getUpdateTime());
        return po;
    }
}
