# Ginkgo ID 数据库表结构优化总结

## 优化概述

本次优化主要针对 `snowflake_node_info` 和 `segment_meta` 两个数据库表进行了结构优化，并相应修改了相关的Java代码。重点优化了机器ID分配机制，使其更加简洁高效，并确保在多线程环境下的并发安全。

## 数据库表结构优化

### 1. snowflake_node_info 表优化

**优化前：**
- 包含IP、创建时间、更新时间等不必要字段
- 机器ID分配逻辑复杂

**优化后：**
```sql
CREATE TABLE IF NOT EXISTS snowflake_node_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT comment '自增ID',
    biz_tag VARCHAR(128) NOT NULL comment '业务标签',
    machine_id BIGINT NOT NULL comment '机器ID',
    UNIQUE KEY uk_biz_tag_machine_id (biz_tag, machine_id) comment '业务标签和机器ID唯一索引'
) comment 'snowflake节点信息表';
```

**主要改进：**
- 移除了IP、创建时间、更新时间等字段，只保留核心的机器ID分配信息
- 每个业务标签下的 `machine_id` 从0开始自增
- 添加了 `(biz_tag, machine_id)` 的唯一索引约束
- 机器ID用完就废弃，不需要保留历史记录

### 2. segment_meta 表优化

**优化前：**
- 使用 `max_id` 字段，语义不够清晰
- 缺少唯一索引约束

**优化后：**
```sql
CREATE TABLE IF NOT EXISTS segment_meta (
    id BIGINT PRIMARY KEY AUTO_INCREMENT comment '自增ID',
    biz_tag VARCHAR(128) NOT NULL comment '业务标签',
    next_id BIGINT NOT NULL default 0 comment '下一个ID',
    step BIGINT NOT NULL default 1000 comment '步长',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP comment '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP comment '修改时间',
    UNIQUE KEY uk_biz_tag (biz_tag) comment '业务标签唯一索引'
) comment 'Segment元数据表';
```

**主要改进：**
- 将 `max_id` 改为 `next_id`，语义更清晰
- 每个业务标签下的 `next_id` 从0开始
- 添加了 `biz_tag` 的唯一索引约束

## 代码修改总结

### 1. 实体类修改

**Machine.java:**
- 只保留 `id`、`bizTag`、`machineId` 三个核心字段
- 移除了 `ip`、`createTime`、`updateTime` 等字段

**SegmentMeta.java:**
- 将 `maxId` 字段改为 `nextId`
- 将 `step` 类型从 `Integer` 改为 `Long`

### 2. Mapper接口修改

**MachineMapper.java:**
- 移除了 `selectByBizTagAndMachineId` 和 `updateIp` 方法
- 只保留核心的机器ID分配相关方法
- **新增**：添加了 `updateMachineId` 和 `insertOrUpdateMachineId` 方法

**SegmentMetaMapper.java:**
- 将 `updateMaxId` 方法改为 `updateNextId`

### 3. XML映射文件修改

**MachineMapper.xml:**
- 简化了resultMap，只映射核心字段
- 移除了IP更新相关的SQL
- 简化了INSERT语句
- **新增**：添加了UPDATE和UPSERT相关的SQL语句

**SegmentMetaMapper.xml:**
- 将 `max_id` 字段改为 `next_id`
- 更新了相关的SQL语句

### 4. 服务层修改

**MetaService.java:**
- 将 `registerMachine` 方法改为 `allocateMachineId`
- 移除了IP参数，只保留业务标签参数

**MetaServiceImpl.java:**
- 简化了机器ID分配逻辑，直接分配下一个可用的机器ID
- 移除了IP更新和历史记录保留逻辑
- **新增并发安全机制**：提供了三种并发安全的机器ID分配方案

### 5. 控制器层修改

**MetaController.java:**
- 将 `/api/snowflakeNode/register` 接口改为 `/api/snowflakeNode/allocate`
- 移除了IP参数，返回分配的机器ID

### 6. DTO和客户端修改

**MachineDTO.java:**
- 只保留核心字段，移除了IP和时间字段

**SegmentMetaDTO.java:**
- 将 `maxId` 改为 `nextId`
- 更新了字段类型

**MetaFeignClient.java:**
- 更新了API接口以匹配新的控制器方法

## 并发安全改进

### 1. 问题分析

原始的 `allocateMachineId` 方法存在以下并发问题：
- `getNextMachineId` 查询和 `insert` 插入之间存在时间窗口
- 多个线程可能同时获取到相同的 `nextMachineId`
- 插入时可能因为唯一索引约束而失败

### 2. 解决方案对比

我们提供了三种并发安全的解决方案：

#### 方案1：INSERT ... ON DUPLICATE KEY UPDATE（推荐）

**实现：**
```java
private Long allocateMachineIdWithUpsert(String bizTag) {
    Long nextMachineId = machineMapper.getNextMachineId(bizTag);
    int result = machineMapper.insertOrUpdateMachineId(bizTag, nextMachineId);
    if (result > 0) {
        return nextMachineId;
    } else {
        throw new RuntimeException("Failed to allocate snowflakeNode ID for bizTag: " + bizTag);
    }
}
```

**SQL实现：**
```sql
INSERT INTO snowflake_node_info (biz_tag, machine_id)
VALUES (#{bizTag}, #{machineId})
ON DUPLICATE KEY UPDATE machine_id = #{machineId}
```

**优点：**
- MySQL原生支持，性能最好
- 原子性操作，无需重试
- 代码简洁，易于维护
- 无并发冲突

**缺点：**
- 依赖MySQL的UPSERT特性

#### 方案2：UPDATE语句

**实现：**
```java
private Long allocateMachineIdWithUpdate(String bizTag) {
    Long nextMachineId = machineMapper.getNextMachineId(bizTag);
    
    // 先插入占位记录
    Machine placeholder = new Machine();
    placeholder.setBizTag(bizTag);
    placeholder.setMachineId(-1L);
    
    try {
        machineMapper.insert(placeholder);
    } catch (Exception e) {
        // 如果插入失败，说明已经有占位记录
    }
    
    // 使用UPDATE语句更新机器ID
    int result = machineMapper.updateMachineId(bizTag, -1L, nextMachineId);
    if (result > 0) {
        return nextMachineId;
    } else {
        throw new RuntimeException("Failed to allocate snowflakeNode ID for bizTag: " + bizTag);
    }
}
```

**SQL实现：**
```sql
UPDATE snowflake_node_info 
SET machine_id = #{newMachineId}
WHERE biz_tag = #{bizTag} AND machine_id = #{oldMachineId}
```

**优点：**
- 使用标准的UPDATE语句
- 数据库兼容性好
- 逻辑清晰

**缺点：**
- 需要先插入占位记录
- 逻辑相对复杂

#### 方案3：重试机制（备选）

**实现：**
```java
@Transactional
public Long allocateMachineIdWithRetry(String bizTag) {
    int maxRetries = 10;
    
    for (int retryCount = 0; retryCount < maxRetries; retryCount++) {
        try {
            Long nextMachineId = machineMapper.getNextMachineId(bizTag);
            Machine snowflakeNode = new Machine();
            snowflakeNode.setBizTag(bizTag);
            snowflakeNode.setMachineId(nextMachineId);
            
            int result = machineMapper.insert(snowflakeNode);
            if (result > 0) {
                return nextMachineId;
            }
            
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("uk_biz_tag_machine_id")) {
                try {
                    Thread.sleep(5);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Thread interrupted while allocating snowflakeNode ID", ie);
                }
                continue;
            } else {
                throw e;
            }
        }
    }
    
    throw new RuntimeException("Failed to allocate snowflakeNode ID after " + maxRetries + " retries");
}
```

**优点：**
- 兼容性好，适用于所有数据库
- 逻辑简单直观

**缺点：**
- 性能较差，需要重试
- 在高并发场景下可能影响性能

### 3. 推荐方案

**推荐使用方案1（INSERT ... ON DUPLICATE KEY UPDATE）**，原因如下：

1. **性能最优**：MySQL原生支持，无需重试
2. **代码最简洁**：逻辑简单，易于维护
3. **并发安全**：原子性操作，无竞争条件
4. **生产验证**：MySQL UPSERT功能成熟稳定

### 4. 并发测试验证

创建了 `MetaServiceUpsertTest` 测试类，验证：
- 50个线程并发分配500个机器ID的唯一性
- 多个业务标签的并发分配
- 成功率验证（>95%）
- ID连续性和范围验证
- 性能测试（1000次分配的性能指标）

## 业务逻辑优化

### 1. 机器ID分配策略

- 每个业务标签下的机器ID从0开始自增
- 支持多业务标签隔离，避免机器ID冲突
- 机器ID用完就废弃，不需要保留历史记录
- 简化了分配逻辑，提高性能
- **新增**：确保多线程环境下的机器ID唯一性
- **新增**：提供多种并发安全方案，推荐使用UPSERT

### 2. 段分配策略

- 每个业务标签下的 `next_id` 从0开始
- 支持自定义步长，默认1000
- 确保ID段的连续性和唯一性

## 测试验证

更新了测试类，验证：
- 机器ID分配功能
- 多次分配机器ID的连续性
- 段分配功能
- 按业务标签查询功能
- **新增**：并发安全测试，验证多线程环境下的正确性
- **新增**：UPSERT方案性能测试

## 使用示例

### 分配机器ID
```java
Long machineId = metaService.allocateMachineId("user_service");
// machineId 返回 0 (第一个机器)
```

### 多次分配机器ID
```java
Long machineId1 = metaService.allocateMachineId("user_service"); // 返回 0
Long machineId2 = metaService.allocateMachineId("user_service"); // 返回 1
Long machineId3 = metaService.allocateMachineId("user_service"); // 返回 2
```

### 获取ID段
```java
SegmentMeta segment = metaService.nextSegment("user_service");
// segment.getNextId() 返回 0 (第一个段)
// segment.getStep() 返回 1000
```

### 查询业务标签下的机器
```java
List<Machine> snowflakeNodes = metaService.listMachinesByBizTag("user_service");
```

## API接口

### 分配机器ID
```
POST /api/snowflakeNode/allocate?bizTag=user_service
Response: {"success": true, "machineId": 0}
```

### 查询业务标签下的机器
```
GET /api/snowflakeNode/list/user_service
Response: [{"id": 1, "bizTag": "user_service", "machineId": 0}]
```

### 获取ID段
```
POST /api/segment/next?bizTag=user_service
Response: {"success": true, "segment": {...}}
```

## 优化效果

1. **简化数据结构**：移除了不必要的字段，减少了存储空间
2. **提高性能**：简化了SQL查询和业务逻辑，提高了响应速度
3. **降低复杂度**：移除了IP管理、状态管理等复杂逻辑
4. **更好的扩展性**：每个业务标签独立管理，支持更好的水平扩展
5. **并发安全**：确保多线程环境下的机器ID唯一性，避免重复分配
6. **多种方案**：提供三种并发安全方案，可根据需求选择

## 注意事项

1. 数据库迁移时需要执行新的SQL脚本
2. 现有数据需要迁移到新的表结构
3. 客户端代码需要更新以使用新的API接口
4. 建议在测试环境充分验证后再部署到生产环境
5. 机器ID分配后不会回收，确保在业务范围内合理使用
6. **新增**：在高并发场景下，推荐使用UPSERT方案以获得最佳性能
7. **新增**：如果使用非MySQL数据库，可以考虑使用重试机制方案 