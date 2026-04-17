package org.aurifolia.cloud.id.metaserver.exception;

/**
 * ID元数据服务异常
 *
 * @author Peng Dan
 * @since 1.0
 */
public class IdMetaException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    public IdMetaException(String message) {
        super(message);
    }
    
    public IdMetaException(String message, Throwable cause) {
        super(message, cause);
    }
}
