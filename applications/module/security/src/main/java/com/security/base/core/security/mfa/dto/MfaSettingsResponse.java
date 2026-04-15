package com.security.base.core.security.mfa.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MfaSettingsResponse {

    private boolean totpEnabled;
    private boolean smsEnabled;
    private boolean emailEnabled;
    private boolean phoneVerified;
    private boolean emailVerified;
    private String preferredFactor;
}

