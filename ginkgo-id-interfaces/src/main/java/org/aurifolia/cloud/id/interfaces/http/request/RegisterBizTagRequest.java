package org.aurifolia.cloud.id.interfaces.http.request;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 注册业务标签请求
 *
 * @author Peng Dan
 * @since 2.0
 */
@Data
public class RegisterBizTagRequest {
    
    /**
     * 业务标签
     */
    @NotBlank(message = "业务标签不能为空")
    private String bizTag;
}
