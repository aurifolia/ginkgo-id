package org.aurifolia.cloud.id.metaserver.mapper;

import org.aurifolia.cloud.id.metaserver.entity.SegmentMeta;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface SegmentMetaMapper {
    SegmentMeta selectByBizTag(@Param("bizTag") String bizTag);
    int insert(SegmentMeta meta);
    int updateNextIdWithVersion(@Param("bizTag") String bizTag, @Param("oldNextId") Long oldNextId, @Param("newNextId") Long newNextId, @Param("updateTime") LocalDateTime updateTime);
} 