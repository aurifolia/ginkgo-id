package org.aurifolia.cloud.id.sdk.http.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * HTTP SDK配置属性
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
     * 是否启用
     */
    private Boolean enabled = false;
}
