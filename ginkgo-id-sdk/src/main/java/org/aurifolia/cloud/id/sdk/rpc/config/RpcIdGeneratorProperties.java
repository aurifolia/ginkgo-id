package org.aurifolia.cloud.id.sdk.rpc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Dubbo RPC SDK配置属性
 *
 * @author Peng Dan
 * @since 2.0
 */
@Data
@ConfigurationProperties(prefix = "ginkgo.id.sdk.rpc")
public class RpcIdGeneratorProperties {
    
    /**
     * 业务标签
     */
    private String bizTag = "default";
    
    /**
     * 步长
     */
    private Long step = 1000L;
    
    /**
     * 是否启用
     */
    private Boolean enabled = false;
    
    /**
     * Dubbo超时时间（毫秒）
     */
    private Integer timeout = 5000;
    
    /**
     * Dubbo重试次数
     */
    private Integer retries = 2;
}
