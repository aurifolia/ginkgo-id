package org.aurifolia.cloud.id.infrastructure.segment.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.aurifolia.cloud.id.infrastructure.segment.po.SegmentMetaPO;

/**
 * 号段元数据Mapper
 *
 * @author Peng Dan
 * @since 2.0
 */
@Mapper
public interface SegmentMetaMapper {
    
    /**
     * 根据业务标签查询（加悲观锁）
     */
    SegmentMetaPO selectByBizTagForUpdate(@Param("bizTag") String bizTag);
    
    /**
     * 根据业务标签查询
     */
    SegmentMetaPO selectByBizTag(@Param("bizTag") String bizTag);
    
    /**
     * 插入号段元数据
     */
    int insert(SegmentMetaPO meta);
    
    /**
     * 更新号段元数据
     */
    int update(SegmentMetaPO meta);
    

}
