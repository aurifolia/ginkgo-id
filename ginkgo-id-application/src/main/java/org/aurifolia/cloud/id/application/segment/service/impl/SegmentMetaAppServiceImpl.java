package org.aurifolia.cloud.id.application.segment.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.aurifolia.cloud.id.application.segment.command.AllocateSegmentCommand;
import org.aurifolia.cloud.id.application.segment.command.SegmentMetaRegisterCommand;
import org.aurifolia.cloud.id.application.segment.converter.SegmentMetaConverter;
import org.aurifolia.cloud.id.application.segment.dto.SegmentMetaDTO;
import org.aurifolia.cloud.id.application.segment.queryservice.SegmentMetaQueryService;
import org.aurifolia.cloud.id.application.segment.service.SegmentMetaAppService;
import org.aurifolia.cloud.id.domain.segment.entity.SegmentMeta;
import org.aurifolia.cloud.id.domain.segment.repository.SegmentMetaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 号段元数据应用服务实现
 *
 * @author Peng Dan
 *
 * @since 2.0
 */
@Slf4j
@Service
public class SegmentMetaAppServiceImpl implements SegmentMetaAppService {
    
    private final SegmentMetaQueryService queryService;
    private final SegmentMetaRepository repository;
    
    public SegmentMetaAppServiceImpl(SegmentMetaQueryService queryService,
                                     SegmentMetaRepository repository) {
        this.queryService = queryService;
        this.repository = repository;
    }
    
    @Override
    @Transactional
    public SegmentMetaDTO allocateSegment(AllocateSegmentCommand command) {
        log.info("分配号段: bizTag={}, step={}", command.getBizTag(), command.getStep());
        
        // 使用悲观锁确保并发安全
        SegmentMeta meta = allocateWithLock(command.getBizTag(), command.getStep());
        
        // 使用MapStruct转换为DTO返回
        return SegmentMetaConverter.INSTANCE.toDTO(meta);
    }
    
    @Override
    @Transactional
    public void registerBizTag(SegmentMetaRegisterCommand command) {
        log.info("注册业务标签: bizTag={}", command.getBizTag());
        
        // 检查是否已存在
        if (repository.findByBizTag(command.getBizTag()).isPresent()) {
            throw new IllegalStateException("bizTag已存在: " + command.getBizTag());
        }
        
        // 创建新号段元数据
        SegmentMeta meta = SegmentMeta.create(command.getBizTag());
        repository.save(meta);
        
        log.info("注册成功: bizTag={}", command.getBizTag());
    }
    
    /**
     * 分配逻辑（使用悲观锁）
     */
    private SegmentMeta allocateWithLock(String bizTag, Long step) {
        // 1. 使用查询服务的悲观锁查询，锁定该行记录
        SegmentMeta meta = queryService.findByBizTagForUpdate(bizTag)
                .orElseThrow(() -> new IllegalStateException("bizTag未预注册: " + bizTag));
        
        // 2. 分配号段（业务规则在Entity中处理）
        meta.allocate(step);
        
        // 由于使用了悲观锁，直接更新即可，不会有并发冲突
        repository.update(meta);
        log.info("更新号段: bizTag={}, nextId={}", meta.getBizTag(), meta.getNextId());
        
        return meta;
    }
}
