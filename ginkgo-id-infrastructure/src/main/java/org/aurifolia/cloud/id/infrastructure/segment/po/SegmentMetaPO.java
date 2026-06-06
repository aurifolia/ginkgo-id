package org.aurifolia.cloud.id.infrastructure.segment.po;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 号段元数据持久化对象
 *
 * @author Peng Dan
 * @since 2.0
 */
@Data
public class SegmentMetaPO {
    
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
