package org.aurifolia.cloud.id.application.snowflake.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Snowflake节点注册命令
 *
 * @author Peng Dan
 * @since 2.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SnowflakeNodeRegisterCommand {
    
    /**
     * 业务标签
     */
    private String bizTag;
}
