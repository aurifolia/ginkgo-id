package org.aurifolia.cloud.id.client.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Configuration;

/**
 * Feign客户端配置
 *
 * @author Peng Dan
 * @since 1.0
 */
@Configuration
@ConditionalOnClass(FeignClient.class)
@EnableFeignClients(basePackages = "org.aurifolia.cloud.id.client.feign")
public class FeignClientConfig {
    // 可扩展自定义配置
}
