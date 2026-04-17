package org.aurifolia.cloud.id.client.provider;

import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboReference;
import org.aurifolia.cloud.common.core.annotation.ConditionalOnPropertyPrefix;
import org.aurifolia.cloud.id.api.dto.SnowflakeNodeDTO;
import org.aurifolia.cloud.id.api.provider.MachineIdProvider;
import org.aurifolia.cloud.id.api.service.IdMetaService;
import org.aurifolia.cloud.id.client.IdGeneratorProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

/**
 * 基于Dubbo的机器ID提供器
 *
 * @author Peng Dan
 * @since 1.0
 */
@Component
@RequiredArgsConstructor
@ConditionalOnClass(DubboReference.class)
@ConditionalOnPropertyPrefix("ginkgo.id.generator.snowflake")
public class DubboMachineIdProvider implements MachineIdProvider {
    private final IdGeneratorProperties properties;
    @DubboReference
    private IdMetaService idMetaService;

    @Override
    public long allocate() {
        SnowflakeNodeDTO snowflakeNodeDTO = idMetaService.nextMachineId(properties.getSnowflake().getBizTag());
        return snowflakeNodeDTO.getMachineId();
    }
}
