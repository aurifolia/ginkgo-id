package org.aurifolia.cloud.id.infrastructure.snowflake.queryservice;

import lombok.RequiredArgsConstructor;
import org.aurifolia.cloud.id.application.snowflake.queryservice.SnowflakeNodeQueryService;
import org.aurifolia.cloud.id.domain.snowflake.entity.SnowflakeNode;
import org.aurifolia.cloud.id.infrastructure.snowflake.mapper.SnowflakeNodeMapper;
import org.aurifolia.cloud.id.infrastructure.snowflake.po.SnowflakeNodePO;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Snowflake节点查询服务实现
 *
 * @author Peng Dan
 * @since 2.0
 */
@Component
@RequiredArgsConstructor
public class SnowflakeNodeQueryServiceImpl implements SnowflakeNodeQueryService {
    
    private final SnowflakeNodeMapper mapper;
    
    @Override
    public Optional<SnowflakeNode> findByBizTagForUpdate(String bizTag) {
        SnowflakeNodePO po = mapper.selectByBizTagForUpdate(bizTag);
        return Optional.ofNullable(po).map(this::convertToEntity);
    }
    
    private SnowflakeNode convertToEntity(SnowflakeNodePO po) {
        SnowflakeNode entity = new SnowflakeNode();
        entity.setId(po.getId());
        entity.setBizTag(po.getBizTag());
        entity.setMachineId(po.getMachineId());
        entity.setCreateTime(po.getCreateTime());
        entity.setUpdateTime(po.getUpdateTime());
        return entity;
    }
}
