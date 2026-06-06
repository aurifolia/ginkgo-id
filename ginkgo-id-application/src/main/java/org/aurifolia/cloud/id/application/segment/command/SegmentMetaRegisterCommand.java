package org.aurifolia.cloud.id.application.segment.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 号段元数据注册命令
 *
 * @author Peng Dan
 * @since 2.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SegmentMetaRegisterCommand {
    
    /**
     * 业务标签
     */
    private String bizTag;
}
