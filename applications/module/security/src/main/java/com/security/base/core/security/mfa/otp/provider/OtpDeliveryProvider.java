package com.security.base.core.security.mfa.otp.provider;

import com.security.base.core.security.mfa.MfaFactorType;

public interface OtpDeliveryProvider {

    MfaFactorType factor();
    void sendOtp(String destination, String message);
}

