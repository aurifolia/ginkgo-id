package org.aurifolia.cloud.id.application.segment.converter;

import org.aurifolia.cloud.id.application.segment.dto.SegmentMetaDTO;
import org.aurifolia.cloud.id.domain.segment.entity.SegmentMeta;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 号段元数据转换器
 *
 * @author Peng Dan
 * @since 2.0
 */
@Mapper
public interface SegmentMetaConverter {
    
    SegmentMetaConverter INSTANCE = Mappers.getMapper(SegmentMetaConverter.class);
    
    /**
     * Entity转DTO
     */
    SegmentMetaDTO toDTO(SegmentMeta entity);
}
