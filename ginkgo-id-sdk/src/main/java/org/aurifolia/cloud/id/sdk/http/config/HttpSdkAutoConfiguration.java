package org.aurifolia.cloud.id.sdk.http.config;

import org.aurifolia.cloud.id.sdk.IdGenerator;
import org.aurifolia.cloud.id.sdk.http.HttpIdGeneratorClient;
import org.aurifolia.cloud.id.sdk.http.feign.IdGeneratorFeignClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * HTTP SDK自动配置类
 *
 * @author Peng Dan
 * @since 2.0
 */
@Configuration
@EnableFeignClients(basePackages = "org.aurifolia.cloud.id.sdk.http.feign")
@EnableConfigurationProperties(IdGeneratorProperties.class)
@ConditionalOnProperty(name = "ginkgo.id.sdk.http.enabled", havingValue = "true", matchIfMissing = false)
public class HttpSdkAutoConfiguration {
    
    /**
     * 创建ID生成器Bean
     */
    @Bean
    @Primary
    public IdGenerator idGenerator(IdGeneratorFeignClient feignClient, 
                                   IdGeneratorProperties properties) {
        return new HttpIdGeneratorClient(
                feignClient, 
                properties.getBizTag(), 
                properties.getStep(),
                properties.getSnowflakeEnabled()
        );
    }
}
