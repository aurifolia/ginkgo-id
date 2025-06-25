-- snowflake节点信息表 - 每个业务标签只允许一行，增加version用于乐观锁
CREATE TABLE IF NOT EXISTS snowflake_node (
    id BIGINT PRIMARY KEY AUTO_INCREMENT comment '自增ID',
    biz_tag VARCHAR(128) not null comment '业务标签',
    machine_id BIGINT NOT NULL comment '机器ID',
    version BIGINT NOT NULL DEFAULT 0 comment '乐观锁版本号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP comment '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP comment '修改时间',
    UNIQUE KEY udx_biz_tag (biz_tag) comment '业务标签唯一索引'
) comment 'snowflake节点表';

-- Segment元数据表
CREATE TABLE IF NOT EXISTS segment_meta (
    id BIGINT PRIMARY KEY AUTO_INCREMENT comment '自增ID',
    biz_tag VARCHAR(128) NOT NULL comment '业务标签',
    next_id BIGINT NOT NULL default 0 comment '下一个ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP comment '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP comment '修改时间',
    UNIQUE KEY udx_biz_tag (biz_tag) comment '业务标签唯一索引'
) comment 'Segment元数据表';