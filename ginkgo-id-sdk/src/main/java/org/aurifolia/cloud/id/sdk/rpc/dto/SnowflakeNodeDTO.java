package org.aurifolia.cloud.id.sdk.rpc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Snowflake节点信息DTO
 *
 * @author Peng Dan
 * @since 2.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SnowflakeNodeDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
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
