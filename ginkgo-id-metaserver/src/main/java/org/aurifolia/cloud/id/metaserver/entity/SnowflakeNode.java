package org.aurifolia.cloud.id.metaserver.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Snowflake节点信息实体
 * <p>
 * 用于存储和管理Snowflake算法的机器ID分配信息
 *
 * @author Peng Dan
 * @since 1.0
 */
@Data
@Accessors(chain = true)
public class SnowflakeNode implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 自增主键ID
     */
    private Long id;
    
    /**
     * 业务标签（唯一标识）
     */
    private String bizTag;
    
    /**
     * 机器ID（0-1023）
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
