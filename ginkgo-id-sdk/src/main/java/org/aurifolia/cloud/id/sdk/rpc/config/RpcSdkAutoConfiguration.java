package org.aurifolia.cloud.id.sdk.rpc.config;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.aurifolia.cloud.id.sdk.IdGenerator;
import org.aurifolia.cloud.id.sdk.rpc.DubboIdGeneratorClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Dubbo RPC SDK自动配置类
 *
 * @author Peng Dan
 * @since 2.0
 */
@Configuration
@EnableDubbo(scanBasePackages = "org.aurifolia.cloud.id.sdk.rpc")
@EnableConfigurationProperties(RpcIdGeneratorProperties.class)
@ConditionalOnProperty(name = "ginkgo.id.sdk.rpc.enabled", havingValue = "true", matchIfMissing = false)
public class RpcSdkAutoConfiguration {
    
    /**
     * 创建基于Dubbo的ID生成器Bean
     */
    @Bean
    public IdGenerator idGenerator(RpcIdGeneratorProperties properties) {
        return new DubboIdGeneratorClient(
                properties.getBizTag(), 
                properties.getStep()
        );
    }
}
