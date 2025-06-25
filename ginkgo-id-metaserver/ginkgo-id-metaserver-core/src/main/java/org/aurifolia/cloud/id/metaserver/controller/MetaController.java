package org.aurifolia.cloud.id.metaserver.controller;

import org.aurifolia.cloud.id.metaserver.common.dto.SegmentMetaDTO;
import org.aurifolia.cloud.id.metaserver.common.dto.SnowflakeNodeDTO;
import org.aurifolia.cloud.id.metaserver.service.MetaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class MetaController {
    @Autowired
    private MetaService metaService;

    @PostMapping("/machine/allocate")
    public SnowflakeNodeDTO allocateMachineId(@RequestParam String bizTag) {
        return metaService.allocateMachineId(bizTag);
    }

    @PostMapping("/segment/next")
    public SegmentMetaDTO nextSegment(@RequestParam String bizTag, @RequestParam Long step) {
        return metaService.nextSegment(bizTag, step);
    }
} 