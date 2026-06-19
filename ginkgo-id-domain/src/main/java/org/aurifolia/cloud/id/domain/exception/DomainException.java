package org.aurifolia.cloud.id.domain.exception;

import lombok.Getter;

/**
 * 领域层基础异常
 *
 * @author Peng Dan
 * @since 2.0
 */
@Getter
public class DomainException extends RuntimeException {

    private final ErrorCode errorCode;

    public DomainException(ErrorCode errorCode, Object... args) {
        super(errorCode.format(args));
        this.errorCode = errorCode;
    }

    public DomainException(ErrorCode errorCode, Throwable cause, Object... args) {
        super(errorCode.format(args), cause);
        this.errorCode = errorCode;
    }
}
