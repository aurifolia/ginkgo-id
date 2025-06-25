package org.aurifolia.cloud.id.metaserver.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aurifolia.cloud.id.metaserver.common.dto.SegmentMetaDTO;
import org.aurifolia.cloud.id.metaserver.common.dto.SnowflakeNodeDTO;
import org.aurifolia.cloud.id.metaserver.entity.SegmentMeta;
import org.aurifolia.cloud.id.metaserver.entity.SnowflakeNode;
import org.aurifolia.cloud.id.metaserver.mapper.SegmentMetaMapper;
import org.aurifolia.cloud.id.metaserver.mapper.SnowflakeNodeMapper;
import org.aurifolia.cloud.id.metaserver.service.MetaService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 元数据服务
 *
 * @author Peng Dan
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MetaServiceImpl implements MetaService {
    private final SnowflakeNodeMapper snowflakeNodeMapper;
    private final SegmentMetaMapper segmentMetaMapper;
    private final RedissonClient redissonClient;

    @Override
    public SnowflakeNodeDTO nextMachineId(String bizTag) {
        String lockKey = "machine_id_lock:" + bizTag;
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock();
        try {
            int maxRetries = 10;
            for (int retry = 0; retry < maxRetries; retry++) {
                SnowflakeNode snowflakeNode = snowflakeNodeMapper.selectByBizTag(bizTag);
                Long oldMachineId = (snowflakeNode == null) ? null : snowflakeNode.getMachineId();
                Long newMachineId = (oldMachineId == null) ? 0L : oldMachineId + 1;
                LocalDateTime now = LocalDateTime.now();
                if (oldMachineId == null) {
                    try {
                        SnowflakeNode newSnowflakeNode = new SnowflakeNode();
                        newSnowflakeNode.setBizTag(bizTag);
                        newSnowflakeNode.setMachineId(newMachineId);
                        newSnowflakeNode.setCreateTime(now);
                        newSnowflakeNode.setUpdateTime(now);
                        snowflakeNodeMapper.insert(newSnowflakeNode);
                        return toSnowflakeNodeDTO(newSnowflakeNode);
                    } catch (DuplicateKeyException e) {
                        log.warn("Duplicate insert for bizTag={}, retrying...", bizTag);
                    }
                } else {
                    int updated = snowflakeNodeMapper.updateMachineId(bizTag, oldMachineId, newMachineId, now);
                    if (updated > 0) {
                        SnowflakeNode updatedNode = snowflakeNodeMapper.selectByBizTag(bizTag);
                        return toSnowflakeNodeDTO(updatedNode);
                    }
                }
                try { Thread.sleep(5); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
            log.error("Failed to allocate machine ID after retries for bizTag: {}", bizTag);
            throw new RuntimeException("Failed to allocate machine ID after retries for bizTag: " + bizTag);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    public SegmentMetaDTO nextSegment(String bizTag, Long step) {
        long finalStep = step == null ? 1000 : step;
        if (finalStep <= 0) {
            throw new IllegalArgumentException("Step must be greater than 0");
        }
        String lockKey = "segment_meta_lock:" + bizTag;
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock();
        try {
            int maxRetries = 10;
            for (int retry = 0; retry < maxRetries; retry++) {
                SegmentMeta meta = segmentMetaMapper.selectByBizTag(bizTag);
                LocalDateTime now = LocalDateTime.now();
                if (meta == null) {
                    meta = new SegmentMeta();
                    meta.setBizTag(bizTag);
                    meta.setNextId(0L);
                    meta.setCreateTime(now);
                    meta.setUpdateTime(now);
                    segmentMetaMapper.insert(meta);
                    SegmentMetaDTO segmentMetaDTO = toSegmentMetaDTO(meta);
                    segmentMetaDTO.setStep(finalStep);
                    return segmentMetaDTO;
                }
                long currentNextId = meta.getNextId();
                long newNextId = currentNextId + finalStep;
                int updated = segmentMetaMapper.updateNextIdWithVersion(bizTag, currentNextId, newNextId, now);
                if (updated > 0) {
                    SegmentMetaDTO result = new SegmentMetaDTO();
                    result.setId(meta.getId());
                    result.setBizTag(meta.getBizTag());
                    result.setNextId(newNextId);
                    result.setStep(finalStep);
                    return result;
                }
                try { Thread.sleep(5); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
            log.error("Failed to allocate segment after retries for bizTag: {}", bizTag);
            throw new RuntimeException("Failed to allocate segment after retries for bizTag: " + bizTag);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private SnowflakeNodeDTO toSnowflakeNodeDTO(SnowflakeNode node) {
        if (node == null) {
            return null;
        }
        return new SnowflakeNodeDTO()
                .setId(node.getId())
                .setBizTag(node.getBizTag())
                .setMachineId(node.getMachineId())
                .setCreateTime(node.getCreateTime())
                .setUpdateTime(node.getUpdateTime());
    }

    private SegmentMetaDTO toSegmentMetaDTO(SegmentMeta meta) {
        if (meta == null) {
            return null;
        }
        return new SegmentMetaDTO()
                .setId(meta.getId())
                .setBizTag(meta.getBizTag())
                .setNextId(meta.getNextId())
                .setCreateTime(meta.getCreateTime())
                .setUpdateTime(meta.getUpdateTime());
    }
} 