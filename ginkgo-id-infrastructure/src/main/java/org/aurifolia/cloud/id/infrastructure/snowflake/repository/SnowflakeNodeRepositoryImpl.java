package org.aurifolia.cloud.id.infrastructure.snowflake.repository;

import lombok.RequiredArgsConstructor;
import org.aurifolia.cloud.id.domain.snowflake.entity.SnowflakeNode;
import org.aurifolia.cloud.id.domain.snowflake.repository.SnowflakeNodeRepository;
import org.aurifolia.cloud.id.infrastructure.snowflake.mapper.SnowflakeNodeMapper;
import org.aurifolia.cloud.id.infrastructure.snowflake.po.SnowflakeNodePO;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Snowflake节点仓储实现
 *
 * @author Peng Dan
 * @since 2.0
 */
@Repository
@RequiredArgsConstructor
public class SnowflakeNodeRepositoryImpl implements SnowflakeNodeRepository {
    
    private final SnowflakeNodeMapper mapper;
    
    @Override
    public Optional<SnowflakeNode> findByBizTag(String bizTag) {
        SnowflakeNodePO po = mapper.selectByBizTag(bizTag);
        return Optional.ofNullable(po).map(this::convertToEntity);
    }
    
    @Override
    public void save(SnowflakeNode node) {
        SnowflakeNodePO po = convertToPO(node);
        mapper.insert(po);
        node.setId(po.getId());
    }
    
    @Override
    public void update(SnowflakeNode node) {
        SnowflakeNodePO po = convertToPO(node);
        mapper.update(po);
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
    
    private SnowflakeNodePO convertToPO(SnowflakeNode entity) {
        SnowflakeNodePO po = new SnowflakeNodePO();
        po.setId(entity.getId());
        po.setBizTag(entity.getBizTag());
        po.setMachineId(entity.getMachineId());
        po.setCreateTime(entity.getCreateTime());
        po.setUpdateTime(entity.getUpdateTime());
        return po;
    }
}
