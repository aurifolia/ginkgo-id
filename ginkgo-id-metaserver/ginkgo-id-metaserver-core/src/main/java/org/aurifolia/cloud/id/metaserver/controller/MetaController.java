package org.aurifolia.cloud.id.metaserver.controller;

import org.aurifolia.cloud.id.metaserver.common.dto.SegmentMetaDTO;
import org.aurifolia.cloud.id.metaserver.common.dto.SnowflakeNodeDTO;
import org.aurifolia.cloud.id.metaserver.common.service.IdMetaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * ID元数据管理
 *
 * @author Peng Dan
 * @since 1.0
 */
@RestController
@RequestMapping("/id-meta")
public class MetaController {
    @Autowired
    private IdMetaService metaService;

    /**
     * 获取snowflake的machine_id
     *
     * @param bizTag 业务标识
     * @return SnowflakeNodeDTO
     */
    @GetMapping("/snowflake/next")
    public SnowflakeNodeDTO nextMachineId(@RequestParam String bizTag) {
        return metaService.nextMachineId(bizTag);
    }

    /**
     * 获取ID段
     *
     * @param bizTag 业务标识
     * @param step 步长
     * @return SegmentMetaDTO
     */
    @GetMapping("/segment/next")
    public SegmentMetaDTO nextSegment(@RequestParam String bizTag, @RequestParam Long step) {
        return metaService.nextSegment(bizTag, step);
    }
} 