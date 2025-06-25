package org.aurifolia.cloud.id.metaserver.client;

import org.aurifolia.cloud.id.metaserver.client.dto.MachineDTO;
import org.aurifolia.cloud.id.metaserver.client.dto.SegmentMetaDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@FeignClient(name = "metaFeignClient", url = "${metaserver.url}")
public interface MetaFeignClient {
    @PostMapping("/api/machine/allocate")
    Map<String, Object> allocateMachineId(@RequestParam("bizTag") String bizTag);

    @PostMapping("/api/segment/next")
    Map<String, Object> nextSegment(@RequestParam("bizTag") String bizTag);
} 