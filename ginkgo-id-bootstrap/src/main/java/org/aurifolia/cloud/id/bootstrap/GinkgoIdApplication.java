package org.aurifolia.cloud.id.bootstrap;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Ginkgo ID服务启动类
 *
 * @author Peng Dan
 * @since 2.0
 */

@EnableDubbo
@SpringBootApplication(scanBasePackages = "org.aurifolia.cloud.id")
@MapperScan("org.aurifolia.cloud.id.infrastructure.*.mapper")
public class GinkgoIdApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(GinkgoIdApplication.class, args);
    }
}
