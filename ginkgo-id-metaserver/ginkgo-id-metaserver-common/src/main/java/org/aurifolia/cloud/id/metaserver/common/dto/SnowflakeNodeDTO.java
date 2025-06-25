package org.aurifolia.cloud.id.metaserver.common.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 机器ID分配DTO
 */
@Data
@Accessors(chain = true)
public class SnowflakeNodeDTO {
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
} 