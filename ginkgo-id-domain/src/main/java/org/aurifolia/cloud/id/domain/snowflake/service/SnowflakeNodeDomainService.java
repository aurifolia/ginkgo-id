package org.aurifolia.cloud.id.domain.snowflake.service;

import lombok.extern.slf4j.Slf4j;
import org.aurifolia.cloud.id.domain.snowflake.entity.SnowflakeNode;
import org.aurifolia.cloud.id.domain.snowflake.repository.SnowflakeNodeRepository;

/**
 * Snowflake节点领域服务
 * <p>
 * 核心业务规则：分配唯一的机器ID
 *
 * @author Peng Dan
 * @since 2.0
 */
@Slf4j
public class SnowflakeNodeDomainService {
    
    private final SnowflakeNodeRepository repository;
    
    public SnowflakeNodeDomainService(SnowflakeNodeRepository repository) {
        this.repository = repository;
    }
    
    /**
     * 分配机器ID
     * 查询最新的SnowflakeNode
     * @return SnowflakeNode对象
     */
    public SnowflakeNode allocateMachineId(String bizTag) {
        // 先查询是否已存在
        return repository.findByBizTag(bizTag)
                .orElseGet(() -> createNewNode(bizTag));
    }
    
    /**
     * 创建新的节点
     */
    private SnowflakeNode createNewNode(String bizTag) {
        // 创建新节点，machineId从0开始
        return SnowflakeNode.create(bizTag, 0L);
    }
}
