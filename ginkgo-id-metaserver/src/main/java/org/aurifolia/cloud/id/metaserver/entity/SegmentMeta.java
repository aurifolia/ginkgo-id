package org.aurifolia.cloud.id.metaserver.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 号段元数据实体
 * <p>
 * 用于存储和管理号段模式ID生成的元数据信息
 *
 * @author Peng Dan
 * @since 1.0
 */
@Data
@Accessors(chain = true)
public class SegmentMeta implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 自增主键ID
     */
    private Long id;
    
    /**
     * 业务标签（唯一标识）
     */
    private String bizTag;
    
    /**
     * 下一个可用ID
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
