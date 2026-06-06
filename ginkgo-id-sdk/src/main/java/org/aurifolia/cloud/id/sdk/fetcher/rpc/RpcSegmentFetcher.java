package org.aurifolia.cloud.id.sdk.fetcher.rpc;

import org.aurifolia.cloud.id.sdk.fetcher.SegmentFetcher;
import org.aurifolia.cloud.id.sdk.rpc.IdMetaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基于Dubbo RPC的号段获取器
 *
 * @author Peng Dan
 * @since 2.0
 */
public class RpcSegmentFetcher implements SegmentFetcher {

    private static final Logger log = LoggerFactory.getLogger(RpcSegmentFetcher.class);

    private final IdMetaService idMetaService;
    private final String bizTag;

    public RpcSegmentFetcher(IdMetaService idMetaService, String bizTag) {
        this.idMetaService = idMetaService;
        this.bizTag = bizTag;
    }

    @Override
    public Long fetchSegment() {
        try {
            return idMetaService.nextSegment(bizTag);
        } catch (Exception e) {
            log.warn("RPC获取号段异常", e);
            return null;
        }
    }
}
