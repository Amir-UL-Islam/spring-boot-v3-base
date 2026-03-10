package com.security.base.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;

/**
 * Auto-configuration for Security Module
 * This enables the security module to be auto-discovered and loaded
 * Note: JPA configuration is handled by DomainConfig
 */
@AutoConfiguration
@ConditionalOnProperty(name = "modules.security.enabled", havingValue = "true", matchIfMissing = true)
@ComponentScan(basePackages = "com.security.base")
public class SecurityModuleAutoConfiguration {

    public SecurityModuleAutoConfiguration() {
        System.out.println("Security Module Auto-Configuration Loaded");
    }
}

