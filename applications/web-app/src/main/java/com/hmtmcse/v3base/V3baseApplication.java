package com.hmtmcse.v3base;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "com.hmtmcse.v3base",      // Main application
        "com.hmtmcse.module",       // Module management system
        "com.security.base",        // Security module
        "com.problemfighter"        // Common utilities
})
public class V3baseApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(V3baseApplication.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.run(args);
    }

}
