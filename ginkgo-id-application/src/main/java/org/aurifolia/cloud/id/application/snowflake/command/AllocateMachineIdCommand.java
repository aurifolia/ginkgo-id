package org.aurifolia.cloud.id.application.snowflake.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分配机器ID命令
 *
 * @author Peng Dan
 * @since 2.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AllocateMachineIdCommand {
    
    /**
     * 业务标签
     */
    private String bizTag;
}
