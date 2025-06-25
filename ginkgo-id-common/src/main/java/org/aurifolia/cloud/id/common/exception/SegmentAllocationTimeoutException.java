package org.aurifolia.cloud.id.common.exception;

/**
 * 段分配超时
 *
 * @author Peng Dan
 * @since 1.0
 */
public class SegmentAllocationTimeoutException extends RuntimeException {
    /**
     * 构造方法
     *
     * @param message 错误信息
     */
    public SegmentAllocationTimeoutException(String message) {
        super(message);
    }
}