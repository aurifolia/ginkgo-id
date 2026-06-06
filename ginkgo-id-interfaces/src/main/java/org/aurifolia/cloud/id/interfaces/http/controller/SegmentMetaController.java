package org.aurifolia.cloud.id.interfaces.http.controller;

import lombok.extern.slf4j.Slf4j;
import org.aurifolia.cloud.common.model.Result;
import org.aurifolia.cloud.id.application.segment.command.AllocateSegmentCommand;
import org.aurifolia.cloud.id.application.segment.command.SegmentMetaRegisterCommand;
import org.aurifolia.cloud.id.application.segment.dto.SegmentMetaDTO;
import org.aurifolia.cloud.id.application.segment.service.SegmentMetaAppService;
import org.aurifolia.cloud.id.interfaces.http.request.AllocateSegmentRequest;
import org.aurifolia.cloud.id.interfaces.http.request.RegisterBizTagRequest;
import org.aurifolia.cloud.id.interfaces.http.response.SegmentMetaResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 号段元数据HTTP接口
 *
 * @author Peng Dan
 * @since 2.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/segment")
public class SegmentMetaController {
    
    private final SegmentMetaAppService appService;
    
    public SegmentMetaController(SegmentMetaAppService appService) {
        this.appService = appService;
    }
    
    /**
     * 注册业务标签
     */
    @PostMapping("/register")
    public Result<Void> registerBizTag(@Validated @RequestBody RegisterBizTagRequest request) {
        log.info("HTTP请求注册业务标签: bizTag={}", request.getBizTag());
        
        // 构建命令
        SegmentMetaRegisterCommand command = new SegmentMetaRegisterCommand(request.getBizTag());
        
        // 调用应用服务
        appService.registerBizTag(command);
        
        return Result.success(null);
    }
    
    /**
     * 分配号段
     */
    @PostMapping("/allocate")
    public Result<SegmentMetaResponse> allocateSegment(
            @Validated @RequestBody AllocateSegmentRequest request) {
        log.info("HTTP请求分配号段: bizTag={}, step={}", request.getBizTag(), request.getStep());
        
        // 构建命令
        AllocateSegmentCommand command = new AllocateSegmentCommand(
                request.getBizTag(), 
                request.getStep()
        );
        
        // 调用应用服务
        SegmentMetaDTO dto = appService.allocateSegment(command);
        
        // 转换为响应对象
        SegmentMetaResponse response = new SegmentMetaResponse();
        BeanUtils.copyProperties(dto, response);
        
        return Result.success(response);
    }
}
