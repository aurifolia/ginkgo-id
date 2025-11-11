package org.aurifolia.cloud.id.metaserver.common.service;

import org.aurifolia.cloud.id.metaserver.common.dto.SegmentMetaDTO;
import org.aurifolia.cloud.id.metaserver.common.dto.SnowflakeNodeDTO;

/**
 * metaserver的client
 *
 * @author Peng Dan
 * @since 1.0
 */
public interface IdMetaService {
    /**
     * 申请snowflake的machine_id
     *
     * @param bizTag 业务标识
     * @return machine_id
     */
    SnowflakeNodeDTO nextMachineId(String bizTag);

    /**
     * 申请segment
     *
     * @param bizTag 业务标识
     * @param step 步长
     * @return segment
     */
    SegmentMetaDTO nextSegment(String bizTag, Long step);
} 