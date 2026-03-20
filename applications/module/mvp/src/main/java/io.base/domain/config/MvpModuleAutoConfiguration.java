package io.base.domain.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;

/**
 * Auto-configuration for MVP Module
 * This enables the MVP module to be auto-discovered and loaded
 * Note: JPA configuration is handled by DomainConfig
 */
@AutoConfiguration
@ConditionalOnProperty(name = "modules.security.enabled", havingValue = "true", matchIfMissing = true)
@ComponentScan(basePackages = "io.base.domain")
public class MvpModuleAutoConfiguration {

    public MvpModuleAutoConfiguration() {
        System.out.println("MVP Module Auto-Configuration Loaded");
    }
}

