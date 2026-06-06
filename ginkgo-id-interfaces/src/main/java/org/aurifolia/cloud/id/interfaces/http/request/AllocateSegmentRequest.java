package org.aurifolia.cloud.id.interfaces.http.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 分配号段请求
 *
 * @author Peng Dan
 * @since 2.0
 */
@Data
public class AllocateSegmentRequest {

    /**
     * 业务标签
     */
    @NotBlank(message = "业务标签不能为空")
    private String bizTag;
}
