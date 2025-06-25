package org.aurifolia.cloud.id.metaserver.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * ID段
 *
 * @author Peng Dan
 * @since 1.0
 */
@Data
@Accessors(chain = true)
public class SegmentMetaDTO {
    /**
     * 自增主键ID
     */
    private Long id;
    /**
     * 业务标签
     */
    private String bizTag;
    /**
     * 下一个ID
     */
    private Long nextId;
    /**
     * 步长
     */
    private Long step;
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