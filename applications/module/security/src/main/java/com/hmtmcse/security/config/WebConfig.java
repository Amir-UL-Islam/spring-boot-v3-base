package com.hmtmcse.security.config;

import com.hmtmcse.security.config.interceptor.AdminAuthenticationHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AdminAuthenticationHandler())
                .addPathPatterns("/api-docs/**");
    }
}

