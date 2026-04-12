package io.base.domain.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Auto-configuration for MVP Module
 * This enables component, entity, and repository scanning for the module.
 */
@AutoConfiguration
@ConditionalOnProperty(name = "modules.security.enabled", havingValue = "true", matchIfMissing = true)
@ComponentScan("io.base.domain")
@EntityScan("io.base.domain")
@EnableJpaRepositories("io.base.domain")
public class MvpModuleAutoConfiguration {

    public MvpModuleAutoConfiguration() {
        System.out.println("MVP Module Auto-Configuration Loaded");
    }
}
