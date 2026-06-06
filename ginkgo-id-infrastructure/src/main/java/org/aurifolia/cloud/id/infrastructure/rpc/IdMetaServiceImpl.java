package org.aurifolia.cloud.id.infrastructure.rpc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.aurifolia.cloud.id.application.segment.command.AllocateSegmentCommand;
import org.aurifolia.cloud.id.application.segment.service.SegmentMetaAppService;
import org.aurifolia.cloud.id.application.snowflake.command.AllocateMachineIdCommand;
import org.aurifolia.cloud.id.application.snowflake.service.SnowflakeNodeAppService;
import org.aurifolia.cloud.id.sdk.rpc.IdMetaService;

/**
 * Dubbo RPC服务提供者实现
 * <p>
 * 将应用层服务暴露为Dubbo RPC接口
 *
 * @author Peng Dan
 * @since 2.0
 */
@Slf4j
@DubboService(timeout = 5000, retries = 2)
@RequiredArgsConstructor
public class IdMetaServiceImpl implements IdMetaService {
    
    private final SnowflakeNodeAppService snowflakeNodeAppService;
    private final SegmentMetaAppService segmentMetaAppService;
    
    @Override
    public Long nextMachineId(String bizTag) {
        try {
            log.debug("Dubbo请求分配机器ID: bizTag={}", bizTag);
            
            AllocateMachineIdCommand command = new AllocateMachineIdCommand();
            command.setBizTag(bizTag);
            
            var result = snowflakeNodeAppService.allocateMachineId(command);
            
            log.debug("成功分配机器ID: bizTag={}, machineId={}", bizTag, result.getMachineId());
            
            return result.getMachineId().longValue();
            
        } catch (Exception e) {
            log.error("分配机器ID失败: bizTag={}", bizTag, e);
            throw e;
        }
    }
    
    @Override
    public Long nextSegment(String bizTag, Long step) {
        try {
            log.debug("Dubbo请求分配号段: bizTag={}, step={}", bizTag, step);
            
            AllocateSegmentCommand command = new AllocateSegmentCommand();
            command.setBizTag(bizTag);
            command.setStep(step);
            
            var result = segmentMetaAppService.allocateSegment(command);
            
            log.debug("成功分配号段: bizTag={}, nextId={}", bizTag, result.getNextId());
            
            return result.getNextId();
            
        } catch (Exception e) {
            log.error("分配号段失败: bizTag={}, step={}", bizTag, step, e);
            throw e;
        }
    }
}
