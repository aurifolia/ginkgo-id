package org.aurifolia.cloud.id.sdk.rpc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.aurifolia.cloud.id.sdk.IdGenerator;

/**
 * 基于Dubbo RPC的ID生成器客户端
 * <p>
 * 通过号段模式生成分布式ID
 *
 * @author Peng Dan
 * @since 2.0
 */
@Slf4j
@RequiredArgsConstructor
public class DubboIdGeneratorClient implements IdGenerator {
    
    @DubboReference(check = false, timeout = 5000, retries = 2)
    private IdMetaService idMetaService;
    
    private final String bizTag;
    private final Long step;
    
    /**
     * 构造函数（使用默认步长1000）
     */
    public DubboIdGeneratorClient(String bizTag) {
        this(bizTag, 1000L);
    }
    
    @Override
    public long nextId() {
        try {
            log.debug("请求分配号段: bizTag={}, step={}", bizTag, step);
            
            // 调用Dubbo服务获取号段起始ID
            Long nextId = idMetaService.nextSegment(bizTag, step);
            
            if (nextId == null) {
                throw new RuntimeException("获取号段失败：返回值为空");
            }
            
            log.debug("成功分配号段: bizTag={}, nextId={}, step={}", bizTag, nextId, step);
            
            return nextId;
            
        } catch (Exception e) {
            log.error("调用Dubbo服务异常: bizTag={}", bizTag, e);
            throw new RuntimeException("生成ID失败", e);
        }
    }
}
