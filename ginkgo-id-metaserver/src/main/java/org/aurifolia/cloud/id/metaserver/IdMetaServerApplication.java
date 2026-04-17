package org.aurifolia.cloud.id.metaserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ID元数据服务启动类
 * <p>
 * 提供分布式ID生成的元数据管理服务，支持：
 * <ul>
 *     <li>Snowflake算法机器ID分配</li>
 *     <li>号段模式ID段分配</li>
 * </ul>
 *
 * @author Peng Dan
 * @since 1.0
 */
@SpringBootApplication
public class IdMetaServerApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(IdMetaServerApplication.class, args);
    }
}
