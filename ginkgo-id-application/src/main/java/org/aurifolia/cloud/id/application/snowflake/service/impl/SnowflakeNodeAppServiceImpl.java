package org.aurifolia.cloud.id.application.snowflake.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.aurifolia.cloud.id.application.snowflake.command.AllocateMachineIdCommand;
import org.aurifolia.cloud.id.application.snowflake.command.SnowflakeNodeRegisterCommand;
import org.aurifolia.cloud.id.application.snowflake.converter.SnowflakeNodeConverter;
import org.aurifolia.cloud.id.application.snowflake.dto.SnowflakeNodeDTO;
import org.aurifolia.cloud.id.application.snowflake.queryservice.SnowflakeNodeQueryService;
import org.aurifolia.cloud.id.application.snowflake.service.SnowflakeNodeAppService;
import org.aurifolia.cloud.id.domain.snowflake.entity.SnowflakeNode;
import org.aurifolia.cloud.id.domain.snowflake.repository.SnowflakeNodeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Snowflake节点应用服务实现
 *
 * @author Peng Dan
 * @since 2.0
 */
@Slf4j
@Service
public class SnowflakeNodeAppServiceImpl implements SnowflakeNodeAppService {
    
    private final SnowflakeNodeQueryService queryService;
    private final SnowflakeNodeRepository repository;
    
    public SnowflakeNodeAppServiceImpl(SnowflakeNodeQueryService queryService,
                                       SnowflakeNodeRepository repository) {
        this.queryService = queryService;
        this.repository = repository;
    }
    
    @Override
    @Transactional
    public SnowflakeNodeDTO allocateMachineId(AllocateMachineIdCommand command) {
        log.info("分配机器ID: bizTag={}", command.getBizTag());
        
        // 使用悲观锁确保并发安全
        SnowflakeNode node = allocateWithLock(command.getBizTag());
        
        // 使用MapStruct转换为DTO返回
        return SnowflakeNodeConverter.INSTANCE.toDTO(node);
    }
    
    @Override
    @Transactional
    public void registerBizTag(SnowflakeNodeRegisterCommand command) {
        log.info("注册业务标签: bizTag={}", command.getBizTag());
        
        // 检查是否已存在
        if (repository.findByBizTag(command.getBizTag()).isPresent()) {
            throw new IllegalStateException("bizTag已存在: " + command.getBizTag());
        }
        
        // 创建新节点（machineId从0开始）
        SnowflakeNode node = SnowflakeNode.create(command.getBizTag(), 0L);
        repository.save(node);
        
        log.info("注册成功: bizTag={}", command.getBizTag());
    }
    
    /**
     * 分配逻辑（使用悲观锁）
     */
    private SnowflakeNode allocateWithLock(String bizTag) {
        // 1. 使用查询服务的悲观锁查询，锁定该行记录
        SnowflakeNode node = queryService.findByBizTagForUpdate(bizTag)
                .orElseThrow(() -> new IllegalStateException("bizTag未预注册: " + bizTag));
        
        // 2. 调整machineId并更新
        Long oldMachineId = node.getMachineId();
        node.prepareNextMachineId();
        Long newMachineId = node.getMachineId();
        
        // 由于使用了悲观锁，直接更新即可，不会有并发冲突
        repository.update(node);
        log.info("更新节点: bizTag={}, oldMachineId={}, newMachineId={}", 
                bizTag, oldMachineId, newMachineId);
        
        return node;
    }
}
