package org.aurifolia.cloud.id.api;

/**
 * ID生成器接口
 *
 * @author Peng Dan
 * @since 1.0
 */
@FunctionalInterface
public interface IdGenerator {
    /**
     * 生成下一个ID
     *
     * @return ID
     */
    long nextId();
}
