package org.aurifolia.cloud.id.client;

import org.aurifolia.cloud.id.common.provider.SegmentProvider;
import org.aurifolia.cloud.id.metaserver.client.MetaFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
@EnableConfigurationProperties(IdGeneratorProperties.class)
public class IdGeneratorAutoConfiguration {
    @Autowired
    private IdGeneratorProperties properties;
    @Autowired(required = false)
    private MetaFeignClient metaFeignClient;

    @Bean
    @ConditionalOnProperty(name = "ginkgo.id.generator.type", havingValue = "snowflake", matchIfMissing = true)
    public IdGenerator snowflakeIdGenerator() {
        Long machineId = properties.getMachineId();
        if (metaFeignClient != null) {
            try {
                // 通过bizTag向metaserver申请machineId
                java.util.Map<String, Object> resp = metaFeignClient.allocateMachineId(properties.getBizTag());
                if (resp != null && resp.get("machineId") != null) {
                    machineId = ((Number) resp.get("machineId")).longValue();
                }
            } catch (Exception e) {
                // 可记录日志，降级使用本地配置
            }
        }
        return new SnowflakeIdGenerator(
                machineId,
                properties.getBufferSize(),
                properties.getFillBatchSize(),
                properties.getMaxIdleTime()
        );
    }

    @Bean
    @ConditionalOnProperty(name = "ginkgo.id.generator.type", havingValue = "segment")
    public IdGenerator segmentIdGenerator(RemoteSegmentProvider remoteSegmentProvider) {
        return new SegmentIdGeneratorImpl(remoteSegmentProvider);
    }
} 