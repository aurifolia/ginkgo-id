package org.aurifolia.cloud.id.domain.snowflake.repository;

import org.aurifolia.cloud.id.domain.snowflake.entity.SnowflakeNode;

import java.util.Optional;

/**
 * Snowflake节点仓储接口
 * <p>
 * 由infrastructure层实现
 *
 * @author Peng Dan
 * @since 2.0
 */
public interface SnowflakeNodeRepository {
    
    /**
     * 根据业务标签查询节点
     */
    Optional<SnowflakeNode> findByBizTag(String bizTag);
    
    /**
     * 保存节点
     */
    void save(SnowflakeNode node);
    
    /**
     * 更新节点
     */
    void update(SnowflakeNode node);
}
