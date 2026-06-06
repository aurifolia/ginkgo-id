package org.aurifolia.cloud.id.common.exception;

/**
 * ID生成服务基础异常
 *
 * @author Peng Dan
 * @since 2.0
 */
public class IdGenerationException extends RuntimeException {
    
    public IdGenerationException(String message) {
        super(message);
    }
    
    public IdGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
