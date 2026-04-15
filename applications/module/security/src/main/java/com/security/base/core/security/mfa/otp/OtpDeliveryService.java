package com.security.base.core.security.mfa.otp;

import com.security.base.core.security.mfa.MfaFactorType;
import com.security.base.core.security.mfa.otp.provider.OtpDeliveryProvider;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class OtpDeliveryService {

    private final Map<MfaFactorType, OtpDeliveryProvider> providers = new EnumMap<>(MfaFactorType.class);

    public OtpDeliveryService(final List<OtpDeliveryProvider> providers) {
        providers.forEach(provider -> this.providers.put(provider.factor(), provider));
    }

    public void sendOtp(final MfaFactorType factor, final String destination, final String message) {
        final OtpDeliveryProvider provider = providers.get(factor);
        if (provider == null) {
            throw new IllegalStateException("No OTP provider configured for factor: " + factor);
        }
        provider.sendOtp(destination, message);
    }
}

