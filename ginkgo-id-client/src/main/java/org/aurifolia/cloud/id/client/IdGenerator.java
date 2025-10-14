package org.aurifolia.cloud.id.client;

/**
 * ID生成器
 *
 * @author Peng Dan
 * @since 1.0
 */
public interface IdGenerator {
    /**
     * 获取ID
     *
     * @return ID
     */
    long nextId();
}