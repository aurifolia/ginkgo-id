package org.aurifolia.cloud.id.infrastructure.config;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan("org.aurifolia.cloud.id.infrastructure")
@EnableJpaRepositories("org.aurifolia.cloud.id.infrastructure")
public class JpaConfig {
}
