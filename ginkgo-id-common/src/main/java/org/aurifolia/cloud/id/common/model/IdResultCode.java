package org.aurifolia.cloud.id.common.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.aurifolia.cloud.common.model.ResultCode;

/**
 * ID服务返回码
 *
 * @author Peng Dan
 * @since 2.0
 */
@Getter
@AllArgsConstructor
public enum IdResultCode implements ResultCode {

    PARAM_ERROR(400, "Parameter validation failed"),
    DOMAIN_ERROR(10001, "Domain error"),
    ID_GENERATION_ERROR(10002, "ID generation failed"),
    DATA_CONFLICT(10003, "Data uniqueness conflict"),
    INTERNAL_ERROR(500, "Internal server error");

    private final Integer code;
    private final String message;
}
