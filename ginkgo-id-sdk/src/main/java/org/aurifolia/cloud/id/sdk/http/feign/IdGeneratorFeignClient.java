package org.aurifolia.cloud.id.sdk.http.feign;

import org.aurifolia.cloud.id.sdk.http.dto.Result;
import org.aurifolia.cloud.id.sdk.http.dto.SegmentAllocateRequest;
import org.aurifolia.cloud.id.sdk.http.dto.SegmentAllocateResponse;
import org.aurifolia.cloud.id.sdk.http.dto.SnowflakeAllocateRequest;
import org.aurifolia.cloud.id.sdk.http.dto.SnowflakeAllocateResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * ID生成服务Feign客户端
 *
 * @author Peng Dan
 * @since 2.0
 */
@FeignClient(name = "ginkgo-id-server", url = "${ginkgo.id.server.url:http://localhost:60101}")
public interface IdGeneratorFeignClient {
    
    /**
     * 分配号段
     *
     * @param request 分配请求
     * @return 分配结果
     */
    @PostMapping("/api/v1/segment/allocate")
    Result<SegmentAllocateResponse> allocateSegment(@RequestBody SegmentAllocateRequest request);
    
    /**
     * 分配Snowflake机器ID
     *
     * @param request 分配请求
     * @return 分配结果
     */
    @PostMapping("/api/v1/snowflake/allocate")
    Result<SnowflakeAllocateResponse> allocateMachineId(@RequestBody SnowflakeAllocateRequest request);
}
