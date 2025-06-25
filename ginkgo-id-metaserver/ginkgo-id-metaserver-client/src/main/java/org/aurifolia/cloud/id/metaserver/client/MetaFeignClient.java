package org.aurifolia.cloud.id.metaserver.client;

import org.aurifolia.cloud.id.metaserver.common.dto.SegmentMetaDTO;
import org.aurifolia.cloud.id.metaserver.common.dto.SnowflakeNodeDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * metaserver的client
 *
 * @author Peng Dan
 * @since 1.0
 */
@FeignClient(name = "metaFeignClient", url = "${metaserver.url}")
@RequestMapping("/id-meta")
public interface MetaFeignClient {
    /**
     * 申请snowflake的machine_id
     *
     * @param bizTag 业务标识
     * @return machine_id
     */
    @PostMapping("/snowflake/next")
    SnowflakeNodeDTO nextMachineId(@RequestParam String bizTag);

    /**
     * 申请segment
     *
     * @param bizTag 业务标识
     * @param step 步长
     * @return segment
     */
    @PostMapping("segment/next")
    SegmentMetaDTO nextSegment(@RequestParam String bizTag, @RequestParam Long step);
} 