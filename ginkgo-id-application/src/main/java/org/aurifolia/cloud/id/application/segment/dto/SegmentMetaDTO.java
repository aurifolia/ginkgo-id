package org.aurifolia.cloud.id.application.segment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 号段元数据DTO
 *
 * @author Peng Dan
 * @since 2.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
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
     * 下一个可用号段编号
     */
    private Long nextSegmentNumber;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
