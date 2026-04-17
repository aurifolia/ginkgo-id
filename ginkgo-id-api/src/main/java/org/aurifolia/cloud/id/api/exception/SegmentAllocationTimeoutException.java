package org.aurifolia.cloud.id.api.exception;

/**
 * 段分配超时异常
 *
 * @author Peng Dan
 * @since 1.0
 */
public class SegmentAllocationTimeoutException extends RuntimeException {
    public SegmentAllocationTimeoutException(String message) {
        super(message);
    }

    public SegmentAllocationTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
