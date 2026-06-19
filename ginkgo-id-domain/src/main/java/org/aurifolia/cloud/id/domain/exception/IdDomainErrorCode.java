package org.aurifolia.cloud.id.domain.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.aurifolia.cloud.common.ddd.ErrorCode;

/**
 * ID领域错误码
 *
 * @author Peng Dan
 * @since 2.0
 */
@Getter
@RequiredArgsConstructor
public enum IdDomainErrorCode implements ErrorCode {

    BIZ_TAG_NOT_FOUND(10001, "bizTag未注册: %s"),
    BIZ_TAG_ALREADY_EXISTS(10002, "bizTag已存在: %s");

    private final int code;
    private final String message;
}
