package com.security.base.core.security.mfa.otp.provider;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "security.mfa.email")
@Getter
@Setter
public class OtpEmailProperties {

    private String from = "amir.ul.islam55971@gmail.com";
    private String subjectPrefix = "[Security OTP]";
}

