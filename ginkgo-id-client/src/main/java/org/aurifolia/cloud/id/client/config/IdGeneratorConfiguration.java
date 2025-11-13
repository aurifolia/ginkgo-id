package org.aurifolia.cloud.id.client.config;

import lombok.RequiredArgsConstructor;
import org.aurifolia.cloud.common.core.annotation.ConditionalOnPropertyPrefix;
import org.aurifolia.cloud.id.client.*;
import org.aurifolia.cloud.id.client.generator.IdGenerator;
import org.aurifolia.cloud.id.client.generator.SegmentIdGeneratorImpl;
import org.aurifolia.cloud.id.client.generator.SnowflakeIdGeneratorImpl;
import org.aurifolia.cloud.id.common.provider.MachineIdProvider;
import org.aurifolia.cloud.id.common.provider.SegmentProvider;
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

    /**
     * 基于snowflake的ID生成器
     *
     * @return IdGenerator
     */
    @Bean
    @Primary
    @ConditionalOnPropertyPrefix("ginkgo.id.generator.snowflake")
    public IdGenerator snowflake(MachineIdProvider machineIdProvider) {
        IdGeneratorProperties.SnowflakeConfig snowflake = properties.getSnowflake();
        return new SnowflakeIdGeneratorImpl(
                machineIdProvider,
                snowflake.getBufferSize(),
                snowflake.getFillBatchSize(),
                snowflake.getMaxIdleTime().toMillis()
        );
    }

    /**
     * 基于ID分段的生成器
     *
     * @param segmentProvider 分段提供者
     * @return IdGenerator
     */
    @Bean
    @ConditionalOnPropertyPrefix("ginkgo.id.generator.segment")
    public IdGenerator segment(SegmentProvider segmentProvider) {
        return new SegmentIdGeneratorImpl(segmentProvider, properties.getSegment().getRingSize());
    }
} 