package org.aurifolia.cloud.id.api.service;

import org.aurifolia.cloud.id.api.dto.SegmentMetaDTO;
import org.aurifolia.cloud.id.api.dto.SnowflakeNodeDTO;

/**
 * ID元数据服务接口（Dubbo RPC）
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
