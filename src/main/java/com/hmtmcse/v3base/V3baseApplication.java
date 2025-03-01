package com.hmtmcse.v3base;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class V3baseApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(V3baseApplication.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.run(args);
    }

}
