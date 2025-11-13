package org.aurifolia.cloud.id.metaserver;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ID metaserver服务
 *
 * @author Peng Dan
 * @since 1.0
 */
@EnableDubbo
@SpringBootApplication
public class IdMetaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(IdMetaServerApplication.class, args);
    }
}
