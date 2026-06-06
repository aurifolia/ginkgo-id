package org.aurifolia.cloud.id.sdk.http.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Snowflake机器ID分配响应
 *
 * @author Peng Dan
 * @since 2.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SnowflakeAllocateResponse {
    
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
