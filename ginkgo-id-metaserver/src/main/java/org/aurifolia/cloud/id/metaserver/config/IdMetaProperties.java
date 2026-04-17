package org.aurifolia.cloud.id.metaserver.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * ID元数据服务配置属性
 *
 * @author Peng Dan
 * @since 1.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "id.meta")
public class IdMetaProperties {
    
    /**
     * 最大重试次数
     */
    private int maxRetries = 3;
    
    /**
     * 重试延迟毫秒数
     */
    private long retryDelayMs = 5;
    
    /**
     * 分布式锁等待时间毫秒数
     */
    private long lockWaitTimeMs = 2000;
    
    /**
     * 分布式锁租约时间秒数
     */
    private long lockLeaseTimeSeconds = 3;
    
    /**
     * 默认步长
     */
    private long defaultStep = 1000L;
}
