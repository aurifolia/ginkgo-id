package org.aurifolia.cloud.id.metaserver.controller;

import lombok.RequiredArgsConstructor;
import org.aurifolia.cloud.id.api.dto.SegmentMetaDTO;
import org.aurifolia.cloud.id.api.dto.SnowflakeNodeDTO;
import org.aurifolia.cloud.id.api.service.IdMetaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 元数据控制器
 *
 * @author Peng Dan
 * @since 1.0
 */
@RestController
@RequestMapping("/id-meta")
@RequiredArgsConstructor
public class MetaController implements IdMetaService {
    private final IdMetaService idMetaService;

    @Override
    @GetMapping("/snowflake/next")
    public SnowflakeNodeDTO nextMachineId(@RequestParam String bizTag) {
        return idMetaService.nextMachineId(bizTag);
    }

    @Override
    @GetMapping("/segment/next")
    public SegmentMetaDTO nextSegment(@RequestParam String bizTag, @RequestParam Long step) {
        return idMetaService.nextSegment(bizTag, step);
    }
}
