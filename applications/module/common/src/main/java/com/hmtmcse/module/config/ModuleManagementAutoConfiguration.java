package com.hmtmcse.module.config;

import com.hmtmcse.module.service.ModuleService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Auto-configuration for module management system
 */
@AutoConfiguration
@ComponentScan(basePackages = "com.hmtmcse.module")
@EntityScan(basePackages = "com.hmtmcse.module.entity")
@EnableJpaRepositories(basePackages = "com.hmtmcse.module.repository")
public class ModuleManagementAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ModuleService moduleService() {
        return new ModuleService();
    }

    @Bean
    public ApplicationListener<ApplicationReadyEvent> moduleDiscoveryListener(ModuleService moduleService) {
        return event -> {
            System.out.println("\n========================================");
            System.out.println("  Module Management System Starting ");
            System.out.println("========================================\n");

            moduleService.discoverAndRegisterModules();

            System.out.println("\n========================================");
            System.out.println("  Active Modules: " + moduleService.getStartedModules().size());
            System.out.println("========================================\n");
        };
    }
}

