package org.aurifolia.cloud.id.metaserver.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aurifolia.cloud.common.model.Result;
import org.aurifolia.cloud.id.api.dto.SegmentMetaDTO;
import org.aurifolia.cloud.id.api.dto.SnowflakeNodeDTO;
import org.aurifolia.cloud.id.api.service.IdMetaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * ID元数据HTTP接口控制器
 * <p>
 * 提供RESTful API供客户端通过HTTP协议获取ID元数据
 *
 * @author Peng Dan
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/id-meta")
@RequiredArgsConstructor
public class MetaController {
    
    private final IdMetaService idMetaService;

    /**
     * 获取下一个Snowflake机器ID
     *
     * @param bizTag 业务标识
     * @return Snowflake节点信息
     */
    @GetMapping("/snowflake/next")
    public Result<SnowflakeNodeDTO> nextMachineId(@RequestParam String bizTag) {
        log.debug("请求分配机器ID: bizTag={}", bizTag);
        return Result.success(idMetaService.nextMachineId(bizTag));
    }

    /**
     * 获取下一个号段
     *
     * @param bizTag 业务标识
     * @param step   步长（可选，默认1000）
     * @return 号段元数据
     */
    @GetMapping("/segment/next")
    public Result<SegmentMetaDTO> nextSegment(
            @RequestParam String bizTag,
            @RequestParam(required = false) Long step) {
        log.debug("请求分配号段: bizTag={}, step={}", bizTag, step);
        return Result.success(idMetaService.nextSegment(bizTag, step));
    }
}
