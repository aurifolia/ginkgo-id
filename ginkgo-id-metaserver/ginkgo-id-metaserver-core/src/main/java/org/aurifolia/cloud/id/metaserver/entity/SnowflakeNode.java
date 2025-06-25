package org.aurifolia.cloud.id.metaserver.entity;

import lombok.Data;
import lombok.experimental.Accessors;
import java.time.LocalDateTime;

/**
 * Snowflake节点信息实体，对应表 snowflake_node_info。
 * 记录每个业务标签下的机器ID分配信息。
 */
@Data
@Accessors(chain = true)
public class SnowflakeNode {
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