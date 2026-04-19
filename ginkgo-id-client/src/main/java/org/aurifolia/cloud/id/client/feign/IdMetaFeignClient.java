package org.aurifolia.cloud.id.client.feign;

import org.aurifolia.cloud.common.model.Result;
import org.aurifolia.cloud.id.api.dto.SegmentMetaDTO;
import org.aurifolia.cloud.id.api.dto.SnowflakeNodeDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * ID元数据Feign客户端
 *
 * @author Peng Dan
 * @since 1.0
 */
@FeignClient(name = "ginkgo-id-metaserver", url = "${ginkgo.id.metaserver.url:}")
public interface IdMetaFeignClient {
    /**
     * 申请snowflake的machine_id
     *
     * @param bizTag 业务标识
     * @return machine_id
     */
    @GetMapping("/id-meta/snowflake/next")
    Result<SnowflakeNodeDTO> nextMachineId(@RequestParam String bizTag);

    /**
     * 申请segment
     *
     * @param bizTag 业务标识
     * @param step 步长
     * @return segment
     */
    @GetMapping("/id-meta/segment/next")
    Result<SegmentMetaDTO> nextSegment(@RequestParam String bizTag, @RequestParam Long step);
}
