package org.aurifolia.cloud.id.infrastructure.rpc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.aurifolia.cloud.id.application.segment.command.AllocateSegmentCommand;
import org.aurifolia.cloud.id.application.segment.service.SegmentMetaAppService;
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

    private final SegmentMetaAppService segmentMetaAppService;

    @Override
    public Long nextSegment(String bizTag) {
        AllocateSegmentCommand command = new AllocateSegmentCommand(bizTag);
        return segmentMetaAppService.allocateSegment(command).getMaxId();
    }
}
