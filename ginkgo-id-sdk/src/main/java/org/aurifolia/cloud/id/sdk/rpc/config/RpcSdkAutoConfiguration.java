package org.aurifolia.cloud.id.sdk.rpc.config;

import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.aurifolia.cloud.id.sdk.IdGenerator;
import org.aurifolia.cloud.id.sdk.fetcher.rpc.RpcSegmentFetcher;
import org.aurifolia.cloud.id.sdk.internal.DegradedIdGenerator;
import org.aurifolia.cloud.id.sdk.internal.SegmentIdGenerator;
import org.aurifolia.cloud.id.sdk.rpc.IdMetaService;
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
@EnableDubbo(scanBasePackages = "org.aurifolia.cloud.id.sdk.rpc.config")
@EnableConfigurationProperties(RpcIdGeneratorProperties.class)
@ConditionalOnProperty(name = "ginkgo.id.sdk.rpc.enabled", havingValue = "true", matchIfMissing = false)
public class RpcSdkAutoConfiguration {

    @DubboReference(check = false, timeout = 5000, retries = 2)
    private IdMetaService idMetaService;

    /**
     * 创建基于Dubbo RPC的ID生成器Bean
     */
    @Bean(destroyMethod = "shutdown")
    public IdGenerator idGenerator(RpcIdGeneratorProperties properties) {
        RpcSegmentFetcher fetcher = new RpcSegmentFetcher(idMetaService, properties.getBizTag());
        DegradedIdGenerator degradedGenerator = new DegradedIdGenerator();
        return new SegmentIdGenerator(fetcher, degradedGenerator, properties.getDegradeEnabled());
    }
}
