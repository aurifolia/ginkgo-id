package org.aurifolia.cloud.id.domain.snowflake.entity;

import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Snowflake节点聚合根
 *
 * @author Peng Dan
 * @since 2.0
 */
@Data
public class  SnowflakeNode {
    private Long id;
    private String bizTag;
    private Long machineId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    /**
     * 创建新的Snowflake节点
     */
    public static SnowflakeNode create(String bizTag, Long machineId) {
        SnowflakeNode node = new SnowflakeNode();
        node.bizTag = bizTag;
        node.machineId = machineId;
        node.createTime = LocalDateTime.now();
        node.updateTime = LocalDateTime.now();
        return node;
    }
    
    /**
     * 准备下一个machineId（调整内部状态）
     * 调用前应先通过 getMachineId() 获取当前值用于CAS
     */
    public void prepareNextMachineId() {
        this.machineId = this.machineId + 1;
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 更新节点信息
     */
    public void update() {
        this.updateTime = LocalDateTime.now();
    }
}
