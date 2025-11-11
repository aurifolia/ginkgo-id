package org.aurifolia.cloud.id.client.config;

import lombok.RequiredArgsConstructor;
import org.aurifolia.cloud.common.core.annotation.ConditionalOnPropertyPrefix;
import org.aurifolia.cloud.id.client.*;
import org.aurifolia.cloud.id.client.generator.IdGenerator;
import org.aurifolia.cloud.id.client.generator.SegmentIdGeneratorImpl;
import org.aurifolia.cloud.id.client.generator.SnowflakeIdGeneratorImpl;
import org.aurifolia.cloud.id.client.provider.HttpSegmentProvider;
import org.aurifolia.cloud.id.metaserver.common.dto.SnowflakeNodeDTO;
import org.aurifolia.cloud.id.metaserver.common.service.IdMetaService;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * ID生成器的配置
 *
 * @author Peng Dan
 * @since 1.0
 */
@Configuration
@EnableAutoConfiguration
@RequiredArgsConstructor
public class IdGeneratorConfiguration {
    private final IdGeneratorProperties properties;
    private final IdMetaService idMetaService;

    /**
     * 基于snowflake的ID生成器
     *
     * @return IdGenerator
     */
    @Bean
    @Primary
    @ConditionalOnPropertyPrefix("ginkgo.id.generator.snowflake")
    public IdGenerator snowflake() {
        IdGeneratorProperties.SnowflakeConfig snowflake = properties.getSnowflake();
        Long machineId = null;
        try {
            // 通过bizTag向metaserver申请machineId
            SnowflakeNodeDTO snowflakeNodeDTO = idMetaService.nextMachineId(snowflake.getBizTag());
            if (snowflakeNodeDTO == null || snowflakeNodeDTO.getMachineId() == null) {
                throw new RuntimeException("allocate machineId failed");
            }
            machineId = snowflakeNodeDTO.getMachineId();
        } catch (Exception e) {
            // 可记录日志，降级使用本地配置
            throw new RuntimeException("allocate machineId failed", e);
        }
        // noinspection DataFlowIssue
        return new SnowflakeIdGeneratorImpl(
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
    @ConditionalOnPropertyPrefix("ginkgo.id.generator.segment")
    public IdGenerator segment(HttpSegmentProvider remoteSegmentProvider) {
        return new SegmentIdGeneratorImpl(remoteSegmentProvider, properties.getSegment().getRingSize());
    }
} 