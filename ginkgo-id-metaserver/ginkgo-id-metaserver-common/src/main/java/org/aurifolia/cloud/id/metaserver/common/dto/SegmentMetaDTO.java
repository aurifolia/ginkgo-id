package org.aurifolia.cloud.id.metaserver.common.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 号段分配DTO
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
    private String createTime;
    /**
     * 更新时间
     */
    private String updateTime;
} 