package org.aurifolia.cloud.id.sdk.http;

import lombok.extern.slf4j.Slf4j;
import org.aurifolia.cloud.id.sdk.IdGenerator;
import org.aurifolia.cloud.id.sdk.http.dto.Result;
import org.aurifolia.cloud.id.sdk.http.dto.SegmentAllocateRequest;
import org.aurifolia.cloud.id.sdk.http.dto.SegmentAllocateResponse;
import org.aurifolia.cloud.id.sdk.http.dto.SnowflakeAllocateRequest;
import org.aurifolia.cloud.id.sdk.http.dto.SnowflakeAllocateResponse;
import org.aurifolia.cloud.id.sdk.http.feign.IdGeneratorFeignClient;

/**
 * 基于OpenFeign的ID生成器客户端
 * <p>
 * 支持两种模式：
 * - Snowflake模式：优先使用，分配machineId后本地生成
 * - 号段模式：降级方案，适用于高并发场景
 * <p>
 * 优先级：如果启用Snowflake模式，则优先使用；否则使用号段模式
 *
 * @author Peng Dan
 * @since 2.0
 */
@Slf4j
public class HttpIdGeneratorClient implements IdGenerator {
    
    private final IdGeneratorFeignClient feignClient;
    private final String bizTag;
    private final Long step;
    private final Boolean snowflakeEnabled;
    
    /**
     * 构造函数（使用默认步长1000，号段模式）
     */
    public HttpIdGeneratorClient(IdGeneratorFeignClient feignClient, String bizTag) {
        this(feignClient, bizTag, 1000L, false);
    }
    
    /**
     * 构造函数（指定步长，号段模式）
     */
    public HttpIdGeneratorClient(IdGeneratorFeignClient feignClient, String bizTag, Long step) {
        this(feignClient, bizTag, step, false);
    }
    
    /**
     * 构造函数（完整参数）
     */
    public HttpIdGeneratorClient(IdGeneratorFeignClient feignClient, String bizTag, Long step, Boolean snowflakeEnabled) {
        this.feignClient = feignClient;
        this.bizTag = bizTag;
        this.step = step;
        this.snowflakeEnabled = snowflakeEnabled != null ? snowflakeEnabled : false;
    }
    
    @Override
    public long nextId() {
        // 优先使用Snowflake模式
        if (snowflakeEnabled) {
            return generateSnowflakeId();
        } else {
            // 降级到号段模式
            return generateSegmentId();
        }
    }
    
    /**
     * 号段模式生成ID
     */
    private long generateSegmentId() {
        try {
            // 构建请求
            SegmentAllocateRequest request = new SegmentAllocateRequest(bizTag, step);
            
            // 调用Feign客户端
            Result<SegmentAllocateResponse> result = feignClient.allocateSegment(request);
            
            // 检查响应
            if (result == null || !result.isSuccess()) {
                String errorMsg = result != null ? result.getMessage() : "响应为空";
                log.error("分配号段失败: bizTag={}, error={}", bizTag, errorMsg);
                throw new RuntimeException("分配号段失败: " + errorMsg);
            }
            
            // 返回下一个ID
            SegmentAllocateResponse response = result.getData();
            if (response == null || response.getNextId() == null) {
                throw new RuntimeException("响应数据为空");
            }
            
            log.debug("成功分配ID: bizTag={}, nextId={}, step={}", 
                    bizTag, response.getNextId(), response.getStep());
            
            return response.getNextId();
            
        } catch (Exception e) {
            log.error("调用ID生成服务异常: bizTag={}", bizTag, e);
            throw new RuntimeException("生成ID失败", e);
        }
    }
    
    /**
     * Snowflake模式生成ID
     * <p>
     * 注意：此实现每次调用都会请求新的machineId，实际生产中应该缓存machineId并在本地生成ID
     */
    private long generateSnowflakeId() {
        try {
            // 构建请求
            SnowflakeAllocateRequest request = new SnowflakeAllocateRequest(bizTag);
            
            // 调用Feign客户端获取machineId
            Result<SnowflakeAllocateResponse> result = feignClient.allocateMachineId(request);
            
            // 检查响应
            if (result == null || !result.isSuccess()) {
                String errorMsg = result != null ? result.getMessage() : "响应为空";
                log.error("分配machineId失败: bizTag={}, error={}", bizTag, errorMsg);
                throw new RuntimeException("分配machineId失败: " + errorMsg);
            }
            
            // 返回machineId（注意：实际生产中应该用machineId在本地生成完整的Snowflake ID）
            SnowflakeAllocateResponse response = result.getData();
            if (response == null || response.getMachineId() == null) {
                throw new RuntimeException("响应数据为空");
            }
            
            log.debug("成功分配machineId: bizTag={}, machineId={}", 
                    bizTag, response.getMachineId());
            
            // 这里返回machineId，实际应该在本地用machineId生成完整的Snowflake ID
            // TODO: 实现本地Snowflake ID生成逻辑
            return response.getMachineId();
            
        } catch (Exception e) {
            log.error("调用ID生成服务异常: bizTag={}", bizTag, e);
            throw new RuntimeException("生成ID失败", e);
        }
    }
}
