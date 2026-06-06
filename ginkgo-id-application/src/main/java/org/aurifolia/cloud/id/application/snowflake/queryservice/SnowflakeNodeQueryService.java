package org.aurifolia.cloud.id.application.snowflake.queryservice;

import org.aurifolia.cloud.id.domain.snowflake.entity.SnowflakeNode;

import java.util.Optional;

/**
 * Snowflake节点查询服务
 * <p>
 * 提供应用层特有的数据查询方法（如悲观锁查询）
 *
 * @author Peng Dan
 * @since 2.0
 */
public interface SnowflakeNodeQueryService {
    
    /**
     * 根据业务标签查询节点（加悲观锁）
     * <p>
     * 使用 SELECT ... FOR UPDATE 锁定行记录
     *
     * @param bizTag 业务标签
     * @return SnowflakeNode
     */
    Optional<SnowflakeNode> findByBizTagForUpdate(String bizTag);
}
