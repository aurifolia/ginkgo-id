package org.aurifolia.cloud.id.sdk.http.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 号段分配请求
 *
 * @author Peng Dan
 * @since 2.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SegmentAllocateRequest {

    /**
     * 业务标签
     */
    private String bizTag;
}
