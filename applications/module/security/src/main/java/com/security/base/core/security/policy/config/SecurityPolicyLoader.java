package com.security.base.core.security.policy.config;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(4)
@Slf4j
@RequiredArgsConstructor
public class SecurityPolicyLoader implements ApplicationRunner {

    private final SecurityPolicyService securityPolicyService;

    @Override
    @Transactional
    public void run(final ApplicationArguments args) {
        log.info("initializing security policy defaults");
        seedDefault(SecurityPolicyKeys.REGISTRATION_ALLOWED_CHANNELS, "SMS,EMAIL");
        seedDefault(SecurityPolicyKeys.REGISTRATION_DEFAULT_CHANNEL, "EMAIL");
        seedDefault(SecurityPolicyKeys.MFA_OPTIONAL_PER_USER, "true");
        seedDefault(SecurityPolicyKeys.MFA_ENFORCED_ROLES, "");
        seedDefault(SecurityPolicyKeys.MFA_ALLOWED_FACTORS_USER, "TOTP,SMS,EMAIL");
        seedDefault(SecurityPolicyKeys.MFA_ALLOWED_FACTORS_ADMIN, "TOTP,EMAIL");
        seedDefault(SecurityPolicyKeys.MFA_ALLOWED_FACTORS_SUPER_ADMIN, "TOTP,EMAIL");
    }

    private void seedDefault(final String key, final String value) {
        final String existing = securityPolicyService.getValueOrDefault(key, null);
        if (existing == null) {
            securityPolicyService.upsert(key, value);
        }
    }
}

