package org.aurifolia.cloud.id.infrastructure.snowflake.po;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Snowflake节点持久化对象
 *
 * @author Peng Dan
 * @since 2.0
 */
@Data
public class SnowflakeNodePO {
    
    /**
     * 自增主键ID
     */
    private Long id;
    
    /**
     * 业务标签
     */
    private String bizTag;
    
    /**
     * 机器ID
     */
    private Long machineId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
