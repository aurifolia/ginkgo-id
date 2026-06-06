package org.aurifolia.cloud.id.domain.segment.repository;

import org.aurifolia.cloud.id.domain.segment.entity.SegmentMeta;

import java.util.Optional;

/**
 * 号段元数据仓储接口
 * <p>
 * 由infrastructure层实现
 *
 * @author Peng Dan
 * @since 2.0
 */
public interface SegmentMetaRepository {

    /**
     * 根据业务标签查询号段元数据
     */
    Optional<SegmentMeta> findByBizTag(String bizTag);

    /**
     * 根据业务标签查询号段元数据（加悲观锁）
     * <p>
     * 使用 SELECT ... FOR UPDATE 锁定行记录
     *
     * @param bizTag 业务标签
     * @return SegmentMeta
     */
    Optional<SegmentMeta> findByBizTagForUpdate(String bizTag);

    /**
     * 保存号段元数据
     */
    void save(SegmentMeta meta);

    /**
     * 更新号段元数据
     */
    void update(SegmentMeta meta);
}
