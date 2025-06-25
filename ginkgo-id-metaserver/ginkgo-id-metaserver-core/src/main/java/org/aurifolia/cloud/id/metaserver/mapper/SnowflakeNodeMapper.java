package org.aurifolia.cloud.id.metaserver.mapper;

import org.aurifolia.cloud.id.metaserver.entity.SnowflakeNode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface SnowflakeNodeMapper {
    /**
     * 根据业务标识查询节点信息
     *
     * @param bizTag 业务标识
     * @return 节点信息
     */
    SnowflakeNode selectByBizTag(@Param("bizTag") String bizTag);

    /**
     * 新增节点信息
     *
     * @param snowflakeNode 节点信息
     * @return 插入记录数
     */
    int insert(SnowflakeNode snowflakeNode);

    /**
     * 更新机器ID
     *
     * @param bizTag 业务标识
     * @param oldMachineId 当前机器ID
     * @param newMachineId 新机器ID
     * @param updateTime 更新时间
     * @return 更新记录数
     */
    int updateMachineId(@Param("bizTag") String bizTag, @Param("oldMachineId") Long oldMachineId,
                        @Param("newMachineId") Long newMachineId, @Param("updateTime") LocalDateTime updateTime);
}