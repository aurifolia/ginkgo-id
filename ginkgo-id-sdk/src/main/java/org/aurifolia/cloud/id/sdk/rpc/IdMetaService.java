package org.aurifolia.cloud.id.sdk.rpc;

import org.aurifolia.cloud.id.sdk.IdGenerator;

/**
 * 基于Dubbo RPC的ID生成器客户端
 *
 * @author Peng Dan
 * @since 2.0
 */
public interface IdMetaService {
    
    /**
     * 申请snowflake的machine_id
     *
     * @param bizTag 业务标识
     * @return machine_id
     */
    Long nextMachineId(String bizTag);
    
    /**
     * 申请segment
     *
     * @param bizTag 业务标识
     * @param step 步长
     * @return segment起始ID
     */
    Long nextSegment(String bizTag, Long step);
}
