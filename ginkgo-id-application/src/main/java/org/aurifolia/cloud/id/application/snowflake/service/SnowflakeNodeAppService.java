package org.aurifolia.cloud.id.application.snowflake.service;

import org.aurifolia.cloud.id.application.snowflake.command.AllocateMachineIdCommand;
import org.aurifolia.cloud.id.application.snowflake.command.SnowflakeNodeRegisterCommand;
import org.aurifolia.cloud.id.application.snowflake.dto.SnowflakeNodeDTO;

/**
 * Snowflake节点应用服务接口
 *
 * @author Peng Dan
 * @since 2.0
 */
public interface SnowflakeNodeAppService {
    
    /**
     * 分配机器ID
     */
    SnowflakeNodeDTO allocateMachineId(AllocateMachineIdCommand command);
    
    /**
     * 注册业务标签
     */
    void registerBizTag(SnowflakeNodeRegisterCommand command);
}
