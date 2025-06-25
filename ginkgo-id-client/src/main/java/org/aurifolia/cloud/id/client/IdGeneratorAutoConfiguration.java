package org.aurifolia.cloud.id.client;

import org.aurifolia.cloud.id.metaserver.client.MetaFeignClient;
import org.aurifolia.cloud.id.metaserver.common.dto.SnowflakeNodeDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ID生成器的配置
 *
 * @author Peng Dan
 * @since 1.0
 */
@Configuration
@EnableAutoConfiguration
@EnableConfigurationProperties(IdGeneratorProperties.class)
public class IdGeneratorAutoConfiguration {
    @Autowired
    private IdGeneratorProperties properties;
    @Autowired(required = false)
    private MetaFeignClient metaFeignClient;

    /**
     * 基于snowflake的ID生成器
     *
     * @return IdGenerator
     */
    @Bean
    @ConditionalOnProperty(name = "ginkgo.id.generator.snowflake")
    public IdGenerator snowflakeIdGenerator() {
        IdGeneratorProperties.SnowflakeConfig snowflake = properties.getSnowflake();
        Long machineId = null;
        if (metaFeignClient != null) {
            try {
                // 通过bizTag向metaserver申请machineId
                SnowflakeNodeDTO snowflakeNodeDTO = metaFeignClient.allocateMachineId(snowflake.getBizTag());
                if (snowflakeNodeDTO == null || snowflakeNodeDTO.getMachineId() == null) {
                    throw new RuntimeException("allocate machineId failed");
                }
                machineId = snowflakeNodeDTO.getMachineId();
            } catch (Exception e) {
                // 可记录日志，降级使用本地配置
                throw new RuntimeException("allocate machineId failed", e);
            }
        }
        // noinspection DataFlowIssue
        return new SnowflakeIdGenerator(
                machineId,
                snowflake.getBufferSize(),
                snowflake.getFillBatchSize(),
                snowflake.getMaxIdleTime().toMillis()
        );
    }

    /**
     * 基于ID分段的生成器
     *
     * @param remoteSegmentProvider 分段提供者
     * @return IdGenerator
     */
    @Bean
    @ConditionalOnProperty(name = "ginkgo.id.generator.segment")
    public IdGenerator segmentIdGenerator(HttpSegmentProvider remoteSegmentProvider) {
        return new SegmentIdGeneratorImpl(remoteSegmentProvider, properties.getSegment().getRingSize());
    }
} 