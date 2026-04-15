package com.security.base.core.security.mfa.otp.dto;

import com.security.base.core.security.mfa.MfaFactorType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OtpChallengeResponse {
    private String challengeId;
    private MfaFactorType factor;
    private String maskedDestination;
    private long expiresInSeconds;
    private String message;
}

