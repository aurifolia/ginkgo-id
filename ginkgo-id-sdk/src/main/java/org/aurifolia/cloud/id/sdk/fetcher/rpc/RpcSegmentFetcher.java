package org.aurifolia.cloud.id.sdk.fetcher.rpc;

import lombok.extern.slf4j.Slf4j;
import org.aurifolia.cloud.id.sdk.fetcher.SegmentFetcher;
import org.aurifolia.cloud.id.sdk.rpc.IdMetaService;

/**
 * 基于Dubbo RPC的号段获取器
 *
 * @author Peng Dan
 * @since 2.0
 */
@Slf4j
public class RpcSegmentFetcher implements SegmentFetcher {

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
            log.warn("RPC segment fetch exception", e);
            return null;
        }
    }
}
