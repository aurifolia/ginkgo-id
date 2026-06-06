package org.aurifolia.cloud.id.application.segment.queryservice;

import org.aurifolia.cloud.id.domain.segment.entity.SegmentMeta;

import java.util.Optional;

/**
 * 号段元数据查询服务
 * <p>
 * 提供应用层特有的数据查询方法（如悲观锁查询）
 *
 * @author Peng Dan
 * @since 2.0
 */
public interface SegmentMetaQueryService {
    
    /**
     * 根据业务标签查询号段元数据（加悲观锁）
     * <p>
     * 使用 SELECT ... FOR UPDATE 锁定行记录
     *
     * @param bizTag 业务标签
     * @return SegmentMeta
     */
    Optional<SegmentMeta> findByBizTagForUpdate(String bizTag);
}
