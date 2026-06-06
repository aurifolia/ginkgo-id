package org.aurifolia.cloud.id.domain.segment.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 号段元数据聚合根
 *
 * @author Peng Dan
 * @since 2.0
 */
@Data
public class SegmentMeta {
    
    /**
     * 默认步长
     */
    private static final Long DEFAULT_STEP = 1000L;
    
    private Long id;
    private String bizTag;
    private Long nextId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    /**
     * 创建新的号段元数据
     */
    public static SegmentMeta create(String bizTag) {
        SegmentMeta meta = new SegmentMeta();
        meta.bizTag = bizTag;
        meta.nextId = 0L;
        meta.createTime = LocalDateTime.now();
        meta.updateTime = LocalDateTime.now();
        return meta;
    }
    
    /**
     * 分配号段（使用指定步长，如果为空或无效则使用默认步长）
     *
     * @param step 步长（可选）
     */
    public void allocate(Long step) {
        // 业务规则：如果步长为空或无效，使用默认步长
        Long actualStep = (step == null || step <= 0) ? DEFAULT_STEP : step;
        
        this.nextId = this.nextId + actualStep;
        this.updateTime = LocalDateTime.now();
    }
}
