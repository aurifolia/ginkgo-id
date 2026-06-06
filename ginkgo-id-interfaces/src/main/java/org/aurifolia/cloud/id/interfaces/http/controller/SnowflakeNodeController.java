package org.aurifolia.cloud.id.interfaces.http.controller;

import lombok.extern.slf4j.Slf4j;
import org.aurifolia.cloud.common.model.Result;
import org.aurifolia.cloud.id.application.snowflake.command.AllocateMachineIdCommand;
import org.aurifolia.cloud.id.application.snowflake.command.SnowflakeNodeRegisterCommand;
import org.aurifolia.cloud.id.application.snowflake.dto.SnowflakeNodeDTO;
import org.aurifolia.cloud.id.application.snowflake.service.SnowflakeNodeAppService;
import org.aurifolia.cloud.id.interfaces.http.request.AllocateMachineIdRequest;
import org.aurifolia.cloud.id.interfaces.http.request.RegisterBizTagRequest;
import org.aurifolia.cloud.id.interfaces.http.response.SnowflakeNodeResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Snowflake节点HTTP接口
 *
 * @author Peng Dan
 * @since 2.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/snowflake")
public class SnowflakeNodeController {
    
    private final SnowflakeNodeAppService appService;
    
    public SnowflakeNodeController(SnowflakeNodeAppService appService) {
        this.appService = appService;
    }
    
    /**
     * 注册业务标签
     */
    @PostMapping("/register")
    public Result<Void> registerBizTag(@Validated @RequestBody RegisterBizTagRequest request) {
        log.info("HTTP请求注册业务标签: bizTag={}", request.getBizTag());
        
        // 构建命令
        SnowflakeNodeRegisterCommand command = new SnowflakeNodeRegisterCommand(request.getBizTag());
        
        // 调用应用服务
        appService.registerBizTag(command);
        
        return Result.success(null);
    }
    
    /**
     * 分配机器ID
     */
    @PostMapping("/allocate")
    public Result<SnowflakeNodeResponse> allocateMachineId(
            @Validated @RequestBody AllocateMachineIdRequest request) {
        log.info("HTTP请求分配机器ID: bizTag={}", request.getBizTag());
        
        // 构建命令
        AllocateMachineIdCommand command = new AllocateMachineIdCommand(request.getBizTag());
        
        // 调用应用服务
        SnowflakeNodeDTO dto = appService.allocateMachineId(command);
        
        // 转换为响应对象
        SnowflakeNodeResponse response = new SnowflakeNodeResponse();
        BeanUtils.copyProperties(dto, response);
        
        return Result.success(response);
    }
}
