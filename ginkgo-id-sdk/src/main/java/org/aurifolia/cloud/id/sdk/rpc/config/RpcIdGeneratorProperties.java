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
     * 是否启用
     */
    private Boolean enabled = false;
}
