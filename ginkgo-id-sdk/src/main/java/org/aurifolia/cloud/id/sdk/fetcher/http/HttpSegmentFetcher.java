package org.aurifolia.cloud.id.sdk.fetcher.http;

import lombok.extern.slf4j.Slf4j;
import org.aurifolia.cloud.id.sdk.fetcher.SegmentFetcher;
import org.aurifolia.cloud.id.sdk.http.dto.Result;
import org.aurifolia.cloud.id.sdk.http.dto.SegmentAllocateRequest;
import org.aurifolia.cloud.id.sdk.http.dto.SegmentAllocateResponse;
import org.aurifolia.cloud.id.sdk.http.feign.IdGeneratorFeignClient;

/**
 * 基于OpenFeign的号段获取器
 *
 * @author Peng Dan
 * @since 2.0
 */
@Slf4j
public class HttpSegmentFetcher implements SegmentFetcher {

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
            log.warn("HTTP segment fetch failed: {}", result != null ? result.getMessage() : "null");
            return null;
        } catch (Exception e) {
            log.warn("HTTP segment fetch exception", e);
            return null;
        }
    }
}
