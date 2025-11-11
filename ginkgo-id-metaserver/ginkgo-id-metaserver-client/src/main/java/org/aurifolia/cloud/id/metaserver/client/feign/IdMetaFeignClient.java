package org.aurifolia.cloud.id.metaserver.client.feign;

import org.aurifolia.cloud.id.metaserver.common.dto.SegmentMetaDTO;
import org.aurifolia.cloud.id.metaserver.common.dto.SnowflakeNodeDTO;
import org.aurifolia.cloud.id.metaserver.common.service.IdMetaService;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * metaserver的client
 *
 * @author Peng Dan
 * @since 1.0
 */
@FeignClient(name = "metaFeignClient", url = "${ginkgo.id.metaserver.url}")
public interface IdMetaFeignClient extends IdMetaService {
    /**
     * 申请snowflake的machine_id
     *
     * @param bizTag 业务标识
     * @return machine_id
     */
    @Override
    @GetMapping("/id-meta/snowflake/next")
    SnowflakeNodeDTO nextMachineId(@RequestParam String bizTag);

    /**
     * 申请segment
     *
     * @param bizTag 业务标识
     * @param step 步长
     * @return segment
     */
    @Override
    @GetMapping("/id-meta/segment/next")
    SegmentMetaDTO nextSegment(@RequestParam String bizTag, @RequestParam Long step);
} 