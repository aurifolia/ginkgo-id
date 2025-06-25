package org.aurifolia.cloud.id.metaserver.client.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

/**
 * 配置FeignClient
 *
 * @author Peng Dan
 * @since 1.0
 */
@Configuration
@EnableFeignClients(basePackages = "org.aurifolia.cloud.id.metaserver.client")
public class FeignConfig {
    // 可扩展自定义配置
} 