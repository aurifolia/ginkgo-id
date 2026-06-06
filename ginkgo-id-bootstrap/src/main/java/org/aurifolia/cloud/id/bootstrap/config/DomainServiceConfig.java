package org.aurifolia.cloud.id.bootstrap.config;

import org.aurifolia.cloud.id.domain.segment.repository.SegmentMetaRepository;
import org.aurifolia.cloud.id.domain.segment.service.SegmentMetaDomainService;
import org.aurifolia.cloud.id.domain.snowflake.repository.SnowflakeNodeRepository;
import org.aurifolia.cloud.id.domain.snowflake.service.SnowflakeNodeDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Domain层服务配置
 * <p>
 * 负责创建domain层的service单例并注入依赖
 *
 * @author Peng Dan
 * @since 2.0
 */
@Configuration
public class DomainServiceConfig {
    
    /**
     * 创建Snowflake节点领域服务
     */
    @Bean
    public SnowflakeNodeDomainService snowflakeNodeDomainService(
            SnowflakeNodeRepository repository) {
        return new SnowflakeNodeDomainService(repository);
    }
    
    /**
     * 创建号段元数据领域服务
     */
    @Bean
    public SegmentMetaDomainService segmentMetaDomainService(
            SegmentMetaRepository repository) {
        return new SegmentMetaDomainService(repository);
    }
}
