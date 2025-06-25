package org.aurifolia.cloud.id.metaserver.entity;

import lombok.Data;
import lombok.experimental.Accessors;
import java.time.LocalDateTime;

/**
 * Segment元数据实体，对应表 segment_meta。
 * 记录每个业务标签下的号段分配信息。
 */
@Data
@Accessors(chain = true)
public class SegmentMeta {
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
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
} 