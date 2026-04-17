package org.aurifolia.cloud.id.client.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

/**
 * ID客户端自动配置
 *
 * @author Peng Dan
 * @since 1.0
 */
@ComponentScan("org.aurifolia.cloud.id.client")
@Import(FeignClientConfig.class)
public class IdClientAutoConfiguration {
}
