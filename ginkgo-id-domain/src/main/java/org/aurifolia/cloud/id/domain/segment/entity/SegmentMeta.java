package org.aurifolia.cloud.id.domain.segment.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 号段元数据聚合根
 *
 * @author Peng Dan
 * @since 2.0
 */
@Getter
public class SegmentMeta {
    @Setter
    private Long id;
    private String bizTag;
    private long nextSegmentNumber;
    private LocalDateTime createTime;
    @Setter
    private LocalDateTime updateTime;

    /**
     * 创建新的号段元数据
     *
     * @param bizTag 业务标签
     */
    public static SegmentMeta create(String bizTag) {
        SegmentMeta meta = new SegmentMeta();
        meta.bizTag = bizTag;
        meta.nextSegmentNumber = 0L;
        meta.createTime = LocalDateTime.now();
        meta.updateTime = LocalDateTime.now();
        return meta;
    }

    /**
     * 从持久化数据重建号段元数据
     */
    public static SegmentMeta reconstitute(Long id, String bizTag, long nextSegmentNumber,
                                           LocalDateTime createTime, LocalDateTime updateTime) {
        SegmentMeta meta = new SegmentMeta();
        meta.id = id;
        meta.bizTag = bizTag;
        meta.nextSegmentNumber = nextSegmentNumber;
        meta.createTime = createTime;
        meta.updateTime = updateTime;
        return meta;
    }

    /**
     * 分配下一个号段编号（步长固定为1）
     */
    public void allocateNextSegment() {
        this.nextSegmentNumber++;
        this.updateTime = LocalDateTime.now();
    }
}
