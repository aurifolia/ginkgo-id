package org.aurifolia.cloud.id.domain.exception;

/**
 * 领域层基础异常
 *
 * @author Peng Dan
 * @since 2.0
 */
public class DomainException extends RuntimeException {
    
    public DomainException(String message) {
        super(message);
    }
    
    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
