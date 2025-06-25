package org.aurifolia.cloud.id.metaserver.service;

import org.aurifolia.cloud.id.metaserver.common.dto.SegmentMetaDTO;
import org.aurifolia.cloud.id.metaserver.common.dto.SnowflakeNodeDTO;

/**
 * 元数据服务
 *
 * @author Peng Dan
 * @since 1.0
 */
public interface MetaService {
    /**
     * 获取snowflake的下一个machineId
     *
     * @param bizTag 业务标识
     * @return SnowflakeNodeDTO
     */
    SnowflakeNodeDTO nextMachineId(String bizTag);

    /**
     * 获取下一个ID段
     *
     * @param bizTag 业务标识
     * @param step 步长
     * @return SegmentMetaDTO
     */
    SegmentMetaDTO nextSegment(String bizTag, Long step);
} 