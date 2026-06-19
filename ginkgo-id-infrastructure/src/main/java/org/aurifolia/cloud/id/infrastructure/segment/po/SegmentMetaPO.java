package org.aurifolia.cloud.id.infrastructure.segment.po;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 号段元数据持久化对象
 *
 * @author Peng Dan
 * @since 2.0
 */
@Data
@Entity
@Table(name = "segment_meta")
public class SegmentMetaPO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "biz_tag", nullable = false, unique = true, length = 64)
    private String bizTag;

    @Column(name = "max_id", nullable = false)
    private Long maxId;

    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;
}
