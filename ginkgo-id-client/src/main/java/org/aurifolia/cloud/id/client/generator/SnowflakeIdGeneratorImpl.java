package org.aurifolia.cloud.id.client.generator;

import org.aurifolia.cloud.id.common.generator.SnowflakeEnhancedGenerator;
import org.aurifolia.cloud.id.common.provider.MachineIdProvider;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * 基于snowflake的ID生成器
 *
 * @author Peng Dan
 * @since 1.0
 */
public class SnowflakeIdGeneratorImpl implements IdGenerator, InitializingBean, DisposableBean {
    private final SnowflakeEnhancedGenerator.IdCache idCache;

    public SnowflakeIdGeneratorImpl(MachineIdProvider machineIdProvider, int bufferSize, int fillBatchSize, long maxIdleTime) {
        this.idCache = new SnowflakeEnhancedGenerator.IdCache(machineIdProvider.allocate(), bufferSize, fillBatchSize, maxIdleTime);
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