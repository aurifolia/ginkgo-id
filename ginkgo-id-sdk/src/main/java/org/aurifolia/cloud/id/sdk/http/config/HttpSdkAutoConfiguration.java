package org.aurifolia.cloud.id.sdk.http.config;

import org.aurifolia.cloud.id.sdk.IdGenerator;
import org.aurifolia.cloud.id.sdk.fetcher.http.HttpSegmentFetcher;
import org.aurifolia.cloud.id.sdk.http.feign.IdGeneratorFeignClient;
import org.aurifolia.cloud.id.sdk.internal.DegradedIdGenerator;
import org.aurifolia.cloud.id.sdk.internal.SegmentIdGenerator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
     * 创建基于HTTP的ID生成器Bean
     */
    @Bean(destroyMethod = "shutdown")
    public IdGenerator idGenerator(IdGeneratorFeignClient feignClient,
                                   IdGeneratorProperties properties) {
        HttpSegmentFetcher fetcher = new HttpSegmentFetcher(feignClient, properties.getBizTag());
        DegradedIdGenerator degradedGenerator = new DegradedIdGenerator();
        return new SegmentIdGenerator(fetcher, degradedGenerator, properties.getDegradeEnabled());
    }
}
