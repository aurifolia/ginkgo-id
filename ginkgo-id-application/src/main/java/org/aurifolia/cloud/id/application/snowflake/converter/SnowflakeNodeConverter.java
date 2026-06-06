package org.aurifolia.cloud.id.application.snowflake.converter;

import org.aurifolia.cloud.id.application.snowflake.dto.SnowflakeNodeDTO;
import org.aurifolia.cloud.id.domain.snowflake.entity.SnowflakeNode;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * Snowflake节点转换器
 *
 * @author Peng Dan
 * @since 2.0
 */
@Mapper
public interface SnowflakeNodeConverter {
    
    SnowflakeNodeConverter INSTANCE = Mappers.getMapper(SnowflakeNodeConverter.class);
    
    /**
     * Entity转DTO
     */
    SnowflakeNodeDTO toDTO(SnowflakeNode entity);
}
