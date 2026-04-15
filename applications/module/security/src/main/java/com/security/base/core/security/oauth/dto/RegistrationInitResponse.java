package com.security.base.core.security.oauth.dto;

import com.security.base.core.security.mfa.MfaFactorType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistrationInitResponse {

    private String challengeId;
    private MfaFactorType channel;
    private String maskedDestination;
    private long expiresInSeconds;
    private String message;
}

