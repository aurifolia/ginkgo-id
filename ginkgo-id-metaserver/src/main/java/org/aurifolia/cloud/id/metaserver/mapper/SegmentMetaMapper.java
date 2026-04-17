package org.aurifolia.cloud.id.metaserver.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.aurifolia.cloud.id.metaserver.entity.SegmentMeta;

import java.time.LocalDateTime;

/**
 * 号段元数据数据访问层
 *
 * @author Peng Dan
 * @since 1.0
 */
@Mapper
public interface SegmentMetaMapper {
    /**
     * 根据业务标识查询号段元数据
     */
    SegmentMeta selectByBizTag(@Param("bizTag") String bizTag);
    
    /**
     * 新增号段元数据
     */
    int insert(SegmentMeta meta);
    
    /**
     * 乐观锁更新nextId
     */
    int updateNextIdWithVersion(@Param("bizTag") String bizTag, @Param("oldNextId") Long oldNextId, 
                                @Param("newNextId") Long newNextId, @Param("updateTime") LocalDateTime updateTime);
}