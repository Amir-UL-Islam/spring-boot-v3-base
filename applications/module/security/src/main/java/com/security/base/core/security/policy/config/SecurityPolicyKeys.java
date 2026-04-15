package com.security.base.core.security.policy.config;

public final class SecurityPolicyKeys {

    private SecurityPolicyKeys() {
    }

    public static final String REGISTRATION_ALLOWED_CHANNELS = "registration.allowed.channels";
    public static final String REGISTRATION_DEFAULT_CHANNEL = "registration.default.channel";
    public static final String MFA_OPTIONAL_PER_USER = "mfa.optional.per.user";
    public static final String MFA_ENFORCED_ROLES = "mfa.enforced.roles";
    public static final String MFA_ALLOWED_FACTORS_USER = "mfa.allowed.factors.user";
    public static final String MFA_ALLOWED_FACTORS_ADMIN = "mfa.allowed.factors.admin";
    public static final String MFA_ALLOWED_FACTORS_SUPER_ADMIN = "mfa.allowed.factors.super_admin";
}

