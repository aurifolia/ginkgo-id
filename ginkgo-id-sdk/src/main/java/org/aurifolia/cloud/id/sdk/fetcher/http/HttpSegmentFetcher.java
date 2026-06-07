package org.aurifolia.cloud.id.sdk.fetcher.http;

import org.aurifolia.cloud.id.sdk.fetcher.SegmentFetcher;
import org.aurifolia.cloud.id.sdk.http.dto.Result;
import org.aurifolia.cloud.id.sdk.http.dto.SegmentAllocateRequest;
import org.aurifolia.cloud.id.sdk.http.dto.SegmentAllocateResponse;
import org.aurifolia.cloud.id.sdk.http.feign.IdGeneratorFeignClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基于OpenFeign的号段获取器
 *
 * @author Peng Dan
 * @since 2.0
 */
public class HttpSegmentFetcher implements SegmentFetcher {

    private static final Logger log = LoggerFactory.getLogger(HttpSegmentFetcher.class);

    private final IdGeneratorFeignClient feignClient;
    private final String bizTag;

    public HttpSegmentFetcher(IdGeneratorFeignClient feignClient, String bizTag) {
        this.feignClient = feignClient;
        this.bizTag = bizTag;
    }

    @Override
    public Long fetchSegment() {
        try {
            Result<SegmentAllocateResponse> result =
                    feignClient.allocateSegment(new SegmentAllocateRequest(bizTag));
            if (result != null && result.isSuccess() && result.getData() != null) {
                return result.getData().getMaxId();
            }
            log.warn("HTTP获取号段失败: {}", result != null ? result.getMessage() : "null");
            return null;
        } catch (Exception e) {
            log.warn("HTTP获取号段异常", e);
            return null;
        }
    }
}
