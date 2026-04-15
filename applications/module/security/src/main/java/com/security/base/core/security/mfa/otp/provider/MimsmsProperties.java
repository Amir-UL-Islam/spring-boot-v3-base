package com.security.base.core.security.mfa.otp.provider;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "mimsms")
@Getter
@Setter
public class MimsmsProperties {
    private String api;
    private String apiKey;
    private String apiSecret;
    private String username;
    private String password;
    private String senderId;
}

