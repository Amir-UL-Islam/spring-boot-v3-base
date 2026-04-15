package com.security.base.core.security.mfa.dto;

import com.security.base.core.security.mfa.MfaFactorType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MfaSettingsRequest {

    private Boolean smsEnabled;
    private Boolean emailEnabled;
    private MfaFactorType preferredFactor;
}

