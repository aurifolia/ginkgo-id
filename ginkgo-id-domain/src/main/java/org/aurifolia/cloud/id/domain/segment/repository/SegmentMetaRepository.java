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
     * 保存号段元数据
     */
    void save(SegmentMeta meta);
    
    /**
     * 更新号段元数据
     */
    void update(SegmentMeta meta);
}
