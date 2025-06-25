package org.aurifolia.cloud.id.client;

import org.aurifolia.cloud.id.common.generator.SnowflakeEnhancedGenerator;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * 基于snowflake的ID生成器
 *
 * @author Peng Dan
 * @since 1.0
 */
public class SnowflakeIdGenerator implements IdGenerator, InitializingBean, DisposableBean {
    private final SnowflakeEnhancedGenerator.IdCache idCache;

    public SnowflakeIdGenerator(long machineId, int bufferSize, int fillBatchSize, long maxIdleTime) {
        this.idCache = new SnowflakeEnhancedGenerator.IdCache(machineId, bufferSize, fillBatchSize, maxIdleTime);
    }

    @Override
    public void afterPropertiesSet() {
        idCache.start();
    }

    @Override
    public void destroy() {
        idCache.shutdown();
    }

    @Override
    public long nextId() {
        return idCache.getId();
    }
} 