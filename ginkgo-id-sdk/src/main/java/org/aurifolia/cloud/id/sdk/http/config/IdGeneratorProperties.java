package org.aurifolia.cloud.id.sdk.http.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ID生成器配置属性
 *
 * @author Peng Dan
 * @since 2.0
 */
@Data
@ConfigurationProperties(prefix = "ginkgo.id.sdk.http")
public class IdGeneratorProperties {
    
    /**
     * 业务标签
     */
    private String bizTag = "default";
    
    /**
     * 步长（号段模式使用）
     */
    private Long step = 1000L;
    
    /**
     * 是否启用Snowflake模式
     * 如果为true，优先使用Snowflake模式；否则使用号段模式
     */
    private Boolean snowflakeEnabled = false;
    
    /**
     * 是否启用
     */
    private Boolean enabled = false;
}
