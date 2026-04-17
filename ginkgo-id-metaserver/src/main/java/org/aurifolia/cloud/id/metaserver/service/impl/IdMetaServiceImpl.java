package org.aurifolia.cloud.id.metaserver.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.aurifolia.cloud.common.utils.RetryFunction;
import org.aurifolia.cloud.id.api.dto.SegmentMetaDTO;
import org.aurifolia.cloud.id.api.dto.SnowflakeNodeDTO;
import org.aurifolia.cloud.id.api.service.IdMetaService;
import org.aurifolia.cloud.id.metaserver.config.IdMetaProperties;
import org.aurifolia.cloud.id.metaserver.entity.SegmentMeta;
import org.aurifolia.cloud.id.metaserver.entity.SnowflakeNode;
import org.aurifolia.cloud.id.metaserver.exception.IdMetaException;
import org.aurifolia.cloud.id.metaserver.mapper.SegmentMetaMapper;
import org.aurifolia.cloud.id.metaserver.mapper.SnowflakeNodeMapper;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * ID元数据服务实现类
 * <p>
 * 核心设计策略：
 * 1. 分布式锁保证并发安全
 * 2. 先查询后插入/更新的通用方案（兼容所有数据库）
 * 3. 重试机制处理并发冲突
 *
 * @author Peng Dan
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@DubboService(interfaceClass = IdMetaService.class)
public class IdMetaServiceImpl implements IdMetaService {
    
    private static final String LOCK_KEY_PREFIX = "id_meta_lock:";
    private static final String MACHINE_ID_LOCK_TYPE = "machine_id";
    private static final String SEGMENT_LOCK_TYPE = "segment";
    private final SnowflakeNodeMapper snowflakeNodeMapper;
    private final SegmentMetaMapper segmentMetaMapper;
    private final RedissonClient redissonClient;
    private final IdMetaProperties properties;

    @Override
    public SnowflakeNodeDTO nextMachineId(String bizTag) {
        return allocateMachineId(bizTag);
    }
    
    public SnowflakeNodeDTO allocateMachineId(String bizTag) {
        validateBizTag(bizTag);
        String lockKey = buildLockKey(MACHINE_ID_LOCK_TYPE, bizTag);
        RLock lock = redissonClient.getLock(lockKey);
        
        try {
            acquireLock(lock, bizTag);
            return doAllocateMachineId(bizTag);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IdMetaException("分配机器ID被中断: bizTag=" + bizTag, e);
        } finally {
            releaseLock(lock);
        }
    }

    /**
     * 执行机器ID分配
     */
    private SnowflakeNodeDTO doAllocateMachineId(String bizTag) {
        LocalDateTime now = LocalDateTime.now();
        
        return RetryFunction.retry(
            properties.getMaxRetries(), 
            properties.getRetryDelayMs(), 
            false, 
            () -> allocateMachineIdInternal(bizTag, now)
        );
    }
    
    /**
     * 机器ID分配内部实现
     * <p>
     * 采用先查询后插入/更新的通用方案，兼容所有数据库
     */
    private SnowflakeNodeDTO allocateMachineIdInternal(String bizTag, LocalDateTime now) {
        // 先查询记录是否存在
        SnowflakeNode existingNode = snowflakeNodeMapper.selectByBizTag(bizTag);
        
        if (existingNode == null) {
            // 记录不存在，创建新记录
            return createNewMachineId(bizTag, now);
        } else {
            // 记录存在，原子递增机器ID
            return incrementExistingMachineId(existingNode, now);
        }
    }
    
    /**
     * 创建新的机器ID记录
     */
    private SnowflakeNodeDTO createNewMachineId(String bizTag, LocalDateTime now) {
        SnowflakeNode newNode = createNewSnowflakeNode(bizTag, now);
        snowflakeNodeMapper.insert(newNode);
        
        log.info("创建新机器ID记录: bizTag={}, machineId=0", bizTag);
        return convertToSnowflakeNodeDTO(newNode);
    }
    
    /**
     * 递增已存在的机器ID
     */
    private SnowflakeNodeDTO incrementExistingMachineId(SnowflakeNode existingNode, LocalDateTime now) {
        Long oldMachineId = existingNode.getMachineId();
        Long newMachineId = oldMachineId + 1;
        
        // 使用乐观锁更新：只有当machine_id未变化时才更新
        int updated = snowflakeNodeMapper.updateMachineId(
            existingNode.getBizTag(), 
            oldMachineId, 
            newMachineId, 
            now
        );
        
        if (updated > 0) {
            log.debug("机器ID递增成功: bizTag={}, machineId: {} -> {}", 
                existingNode.getBizTag(), oldMachineId, newMachineId);
            // 更新本地对象，避免额外查询
            existingNode.setMachineId(newMachineId);
            existingNode.setUpdateTime(now);
            return convertToSnowflakeNodeDTO(existingNode);
        }
        
        // 更新失败（并发冲突），抛出异常触发重试
        throw new IdMetaException("机器ID更新失败（并发冲突）: bizTag=" + existingNode.getBizTag());
    }



    @Override
    public SegmentMetaDTO nextSegment(String bizTag, Long step) {
        return allocateSegment(bizTag, step);
    }
    
    public SegmentMetaDTO allocateSegment(String bizTag, Long step) {
        validateBizTag(bizTag);
        long normalizedStep = normalizeStep(step);
        String lockKey = buildLockKey(SEGMENT_LOCK_TYPE, bizTag);
        RLock lock = redissonClient.getLock(lockKey);
        
        try {
            acquireLock(lock, bizTag);
            return doAllocateSegment(bizTag, normalizedStep);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IdMetaException("分配号段被中断: bizTag=" + bizTag, e);
        } finally {
            releaseLock(lock);
        }
    }

    /**
     * 规范化步长参数
     */
    private long normalizeStep(Long step) {
        if (step == null || step <= 0) {
            return properties.getDefaultStep();
        }
        return step;
    }

    /**
     * 执行号段分配
     */
    private SegmentMetaDTO doAllocateSegment(String bizTag, long step) {
        LocalDateTime now = LocalDateTime.now();
        
        return RetryFunction.retry(
            properties.getMaxRetries(), 
            properties.getRetryDelayMs(), 
            false, 
            () -> allocateSegmentInternal(bizTag, step, now)
        );
    }
    
    /**
     * 号段分配内部实现
     * <p>
     * 采用先查询后插入/更新的通用方案，兼容所有数据库
     */
    private SegmentMetaDTO allocateSegmentInternal(String bizTag, long step, LocalDateTime now) {
        // 先查询记录是否存在
        SegmentMeta meta = segmentMetaMapper.selectByBizTag(bizTag);
        
        if (meta == null) {
            // 记录不存在，创建新记录
            return createNewSegment(bizTag, step, now);
        } else {
            // 记录存在，更新nextId
            return updateExistingSegment(meta, step, now);
        }
    }
    
    /**
     * 创建新的号段记录
     */
    private SegmentMetaDTO createNewSegment(String bizTag, long step, LocalDateTime now) {
        SegmentMeta newMeta = createNewSegmentMeta(bizTag, now);
        segmentMetaMapper.insert(newMeta);
        
        log.info("创建新号段记录: bizTag={}, nextId=0, step={}", bizTag, step);
        return buildSegmentMetaDTO(newMeta, step);
    }
    
    /**
     * 更新已存在的号段记录
     */
    private SegmentMetaDTO updateExistingSegment(SegmentMeta meta, long step, LocalDateTime now) {
        long currentNextId = meta.getNextId();
        long newNextId = currentNextId + step;
        
        // 乐观锁更新：只有当next_id未变化时才更新
        int updated = segmentMetaMapper.updateNextIdWithVersion(
            meta.getBizTag(), 
            currentNextId, 
            newNextId, 
            now
        );
        
        if (updated > 0) {
            log.debug("号段更新成功: bizTag={}, nextId: {} -> {}", 
                meta.getBizTag(), currentNextId, newNextId);
            // 构建结果，使用新的nextId
            return buildSegmentMetaDTO(meta, newNextId, step);
        }
        
        // 更新失败（并发冲突），抛出异常触发重试
        throw new IdMetaException("号段更新失败（并发冲突）: bizTag=" + meta.getBizTag());
    }



    /**
     * 构建分布式锁键
     */
    private String buildLockKey(String type, String bizTag) {
        return LOCK_KEY_PREFIX + type + ":" + bizTag;
    }
    
    /**
     * 获取分布式锁
     */
    private void acquireLock(RLock lock, String bizTag) throws InterruptedException {
        boolean locked = lock.tryLock(
            properties.getLockWaitTimeMs(), 
            properties.getLockLeaseTimeSeconds(), 
            TimeUnit.MILLISECONDS
        );
        if (!locked) {
            throw new IdMetaException("获取分布式锁超时: bizTag=" + bizTag);
        }
    }

    /**
     * 释放分布式锁
     */
    private void releaseLock(RLock lock) {
        if (lock != null && lock.isHeldByCurrentThread()) {
            try {
                lock.unlock();
            } catch (Exception e) {
                log.warn("释放分布式锁异常", e);
            }
        }
    }
    
    /**
     * 验证业务标签
     */
    private void validateBizTag(String bizTag) {
        if (bizTag == null || bizTag.trim().isEmpty()) {
            throw new IdMetaException("业务标签不能为空");
        }
    }
    
    /**
     * 创建新的Snowflake节点实体
     */
    private SnowflakeNode createNewSnowflakeNode(String bizTag, LocalDateTime now) {
        SnowflakeNode node = new SnowflakeNode();
        node.setBizTag(bizTag);
        node.setMachineId(0L);
        node.setCreateTime(now);
        node.setUpdateTime(now);
        return node;
    }
    
    /**
     * 创建新的号段元数据实体
     */
    private SegmentMeta createNewSegmentMeta(String bizTag, LocalDateTime now) {
        SegmentMeta meta = new SegmentMeta();
        meta.setBizTag(bizTag);
        meta.setNextId(0L);
        meta.setCreateTime(now);
        meta.setUpdateTime(now);
        return meta;
    }

    /**
     * 转换Snowflake节点实体为DTO
     */
    private SnowflakeNodeDTO convertToSnowflakeNodeDTO(SnowflakeNode node) {
        SnowflakeNodeDTO dto = new SnowflakeNodeDTO();
        dto.setId(node.getId());
        dto.setBizTag(node.getBizTag());
        dto.setMachineId(node.getMachineId());
        dto.setCreateTime(node.getCreateTime());
        dto.setUpdateTime(node.getUpdateTime());
        return dto;
    }

    /**
     * 转换号段元数据实体为DTO（不含step）
     */
    private SegmentMetaDTO convertToSegmentMetaDTO(SegmentMeta meta) {
        SegmentMetaDTO dto = new SegmentMetaDTO();
        dto.setId(meta.getId());
        dto.setBizTag(meta.getBizTag());
        dto.setNextId(meta.getNextId());
        dto.setCreateTime(meta.getCreateTime());
        dto.setUpdateTime(meta.getUpdateTime());
        return dto;
    }
    
    /**
     * 构建号段DTO（含step）
     */
    private SegmentMetaDTO buildSegmentMetaDTO(SegmentMeta meta, long step) {
        SegmentMetaDTO dto = convertToSegmentMetaDTO(meta);
        dto.setStep(step);
        return dto;
    }
    
    /**
     * 构建号段DTO（使用新的nextId）
     */
    private SegmentMetaDTO buildSegmentMetaDTO(SegmentMeta meta, long newNextId, long step) {
        SegmentMetaDTO dto = convertToSegmentMetaDTO(meta);
        dto.setNextId(newNextId);
        dto.setStep(step);
        return dto;
    }
}