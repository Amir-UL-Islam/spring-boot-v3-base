package com.security.base.core.security.two_fa;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import org.springframework.stereotype.Service;

@Service
public class TwoFactorService {

    private final GoogleAuthenticator googleAuthenticator;

    public TwoFactorService() {
        final GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder builder =
                new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder();
        this.googleAuthenticator = new GoogleAuthenticator(builder.build());
    }

    public GoogleAuthenticatorKey generateSecret() {
        return googleAuthenticator.createCredentials();
    }

    public boolean isCodeValid(final String secret, final String code) {
        if (secret == null || code == null || code.isBlank()) {
            return false;
        }
        try {
            final int codeInt = Integer.parseInt(code);
            return googleAuthenticator.authorize(secret, codeInt);
        } catch (NumberFormatException ex) {
            return false;
        }
    }
}

