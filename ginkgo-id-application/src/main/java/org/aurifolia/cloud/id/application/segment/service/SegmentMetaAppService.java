package org.aurifolia.cloud.id.application.segment.service;

import org.aurifolia.cloud.id.application.segment.command.AllocateSegmentCommand;
import org.aurifolia.cloud.id.application.segment.command.SegmentMetaRegisterCommand;
import org.aurifolia.cloud.id.application.segment.dto.SegmentMetaDTO;

/**
 * 号段元数据应用服务接口
 *
 * @author Peng Dan
 * @since 2.0
 */
public interface SegmentMetaAppService {
    
    /**
     * 分配号段
     */
    SegmentMetaDTO allocateSegment(AllocateSegmentCommand command);
    
    /**
     * 注册业务标签
     */
    void registerBizTag(SegmentMetaRegisterCommand command);
}
