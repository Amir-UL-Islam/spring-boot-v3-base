package com.security.base.core.security.mfa.otp.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "security.mfa.otp")
@Getter
@Setter
public class OtpProperties {

    private int ttlSeconds = 300;
    private int maxAttempts = 5;
    private int codeLength = 6;
    private int resendCooldownSeconds = 30;
}

