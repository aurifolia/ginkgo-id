package org.aurifolia.cloud.id.infrastructure.snowflake.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.aurifolia.cloud.id.infrastructure.snowflake.po.SnowflakeNodePO;

/**
 * Snowflake节点Mapper
 *
 * @author Peng Dan
 * @since 2.0
 */
@Mapper
public interface SnowflakeNodeMapper {
    
    /**
     * 根据业务标签查询（加悲观锁）
     */
    SnowflakeNodePO selectByBizTagForUpdate(@Param("bizTag") String bizTag);
    
    /**
     * 根据业务标签查询
     */
    SnowflakeNodePO selectByBizTag(@Param("bizTag") String bizTag);
    
    /**
     * 插入节点
     */
    int insert(SnowflakeNodePO node);
    
    /**
     * 更新节点
     */
    int update(SnowflakeNodePO node);
    

}
