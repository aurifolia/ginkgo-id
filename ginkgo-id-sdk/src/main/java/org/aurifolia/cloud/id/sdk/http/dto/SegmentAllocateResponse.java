package org.aurifolia.cloud.id.sdk.http.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 号段分配响应
 *
 * @author Peng Dan
 * @since 2.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SegmentAllocateResponse {

    /**
     * 自增主键ID
     */
    private Long id;

    /**
     * 业务标签
     */
    private String bizTag;

    /**
     * 当前已分配的最大号段编号
     */
    private Long maxId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
