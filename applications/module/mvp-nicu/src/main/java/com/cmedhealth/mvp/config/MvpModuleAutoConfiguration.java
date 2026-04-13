package com.cmedhealth.mvp.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Autoconfiguration for MVP Module
 * This enables component, entity, and repository scanning for the module.
 */
@AutoConfiguration
@ConditionalOnProperty(name = "modules.security.enabled", havingValue = "true", matchIfMissing = true)
@ComponentScan("com.cmedhealth.mvp")
@EntityScan("com.cmedhealth.mvp")
@EnableJpaRepositories("com.cmedhealth.mvp")
public class MvpModuleAutoConfiguration {

    public MvpModuleAutoConfiguration() {
        System.out.println("MVP Module Auto-Configuration Loaded");
    }
}
