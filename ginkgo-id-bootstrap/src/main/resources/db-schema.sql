-- 创建数据库
CREATE DATABASE IF NOT EXISTS ginkgo_id DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE ginkgo_id;

-- Snowflake节点表
CREATE TABLE IF NOT EXISTS snowflake_node (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '自增主键ID',
    biz_tag VARCHAR(64) NOT NULL UNIQUE COMMENT '业务标签',
    machine_id BIGINT NOT NULL COMMENT '机器ID（0-1023）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_biz_tag (biz_tag),
    INDEX idx_machine_id (machine_id),
    -- 添加联合唯一索引，确保同一bizTag下machineId的唯一性（用于并发控制）
    UNIQUE KEY uk_biz_tag_machine_id (biz_tag, machine_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Snowflake节点信息表';

-- 号段元数据表
CREATE TABLE IF NOT EXISTS segment_meta (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '自增主键ID',
    biz_tag VARCHAR(64) NOT NULL UNIQUE COMMENT '业务标签',
    next_id BIGINT NOT NULL DEFAULT 0 COMMENT '下一个可用ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_biz_tag (biz_tag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='号段元数据表';
