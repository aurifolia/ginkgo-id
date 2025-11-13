package org.aurifolia.cloud.id.metaserver.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * snowflake节点信息
 *
 * @author Peng Dan
 * @since 1.0
 */
@Data
@Accessors(chain = true)
public class SnowflakeNodeDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = -7533093412690213226L;
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
} 