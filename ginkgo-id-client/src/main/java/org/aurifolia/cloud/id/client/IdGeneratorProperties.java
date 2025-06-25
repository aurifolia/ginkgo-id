package org.aurifolia.cloud.id.client;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * ID生成器的配置
 *
 * @author Peng Dan
 * @since 1.0
 */
@Data
@Component
@ConditionalOnProperty(name = "ginkgo.id.generator.enable", havingValue = "true")
@ConfigurationProperties(prefix = "ginkgo.id.generator")
public class IdGeneratorProperties {
    /**
     * 是否启用
     */
    private boolean enable;
    /**
     * 基于Snowflake的生成器配置
     */
    private SnowflakeConfig snowflake;
    /**
     * 基于Segment的生成器配置
     */
    private SegmentConfig segment;

    /**
     * 基于Segment的生成器配置
     */
    @Data
    public static class SegmentConfig {
        /**
         * 业务标识
         */
        private String bizTag;
        /**
         * ID段的步长
         */
        private long step = 10_000L;
        /**
         * 段的容量
         */
        private int ringSize = 8;
    }

    /**
     * 基于Snowflake的生成器配置
     */
    @Data
    public static class SnowflakeConfig {
        /**
         * 业务标识
         */
        private String bizTag;
        /**
         * 唤醒缓冲容量
         */
        private int bufferSize = 1 << 20;
        /**
         * 每次填充的ID数量
         */
        private int fillBatchSize = 1 << 14;
        /**
         * 生产者线程最大的空闲时间
         */
        private Duration maxIdleTime = Duration.ofSeconds(5);
    }
}