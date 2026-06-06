package org.aurifolia.cloud.id.sdk.rpc;

/**
 * ID元数据Dubbo RPC服务接口
 *
 * @author Peng Dan
 * @since 2.0
 */
public interface IdMetaService {

    /**
     * 申请segment号段编号
     *
     * @param bizTag 业务标识
     * @return 号段编号
     */
    Long nextSegment(String bizTag);
}
