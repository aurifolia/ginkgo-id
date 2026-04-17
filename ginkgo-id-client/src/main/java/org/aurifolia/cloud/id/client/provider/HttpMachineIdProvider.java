package org.aurifolia.cloud.id.client.provider;

import lombok.RequiredArgsConstructor;
import org.aurifolia.cloud.common.core.annotation.ConditionalOnPropertyPrefix;
import org.aurifolia.cloud.id.api.dto.SnowflakeNodeDTO;
import org.aurifolia.cloud.id.api.provider.MachineIdProvider;
import org.aurifolia.cloud.id.client.IdGeneratorProperties;
import org.aurifolia.cloud.id.api.service.IdMetaService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * 通过http获取machineId
 *
 * @author Peng Dan
 * @since 1.0
 */
@Primary
@Component
@RequiredArgsConstructor
@ConditionalOnBean(IdMetaService.class)
@ConditionalOnPropertyPrefix("ginkgo.id.generator.snowflake")
public class HttpMachineIdProvider implements MachineIdProvider {
    private final IdGeneratorProperties properties;
    private final IdMetaService idMetaService;

    @Override
    public long allocate() {
        SnowflakeNodeDTO snowflakeNodeDTO = idMetaService.nextMachineId(properties.getSnowflake().getBizTag());
        return snowflakeNodeDTO.getMachineId();
    }
}
