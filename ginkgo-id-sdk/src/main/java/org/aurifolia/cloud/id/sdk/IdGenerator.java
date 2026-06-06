package org.aurifolia.cloud.id.sdk;

/**
 * ID生成器接口
 * <p>
 * 供系统内其他微服务使用
 *
 * @author Peng Dan
 * @since 2.0
 */
public interface IdGenerator {
    
    /**
     * 生成下一个ID
     *
     * @return ID
     */
    long nextId();
}
