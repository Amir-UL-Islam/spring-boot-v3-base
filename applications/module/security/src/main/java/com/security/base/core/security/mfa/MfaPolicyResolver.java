package com.security.base.core.security.mfa;

import com.security.base.core.security.oauth.UserRoles;
import com.security.base.core.security.policy.config.SecurityPolicyKeys;
import com.security.base.core.security.policy.config.SecurityPolicyService;
import com.security.base.core.users.model.entity.Users;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MfaPolicyResolver {

    private final SecurityPolicyService securityPolicyService;

    public Set<MfaFactorType> allowedRegistrationChannels() {
        return parseFactors(securityPolicyService.getCsvOrDefault(
                SecurityPolicyKeys.REGISTRATION_ALLOWED_CHANNELS,
                "SMS,EMAIL"
        )).stream().filter(factor -> factor == MfaFactorType.SMS || factor == MfaFactorType.EMAIL)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    public MfaFactorType defaultRegistrationChannel() {
        final String value = securityPolicyService.getValueOrDefault(
                SecurityPolicyKeys.REGISTRATION_DEFAULT_CHANNEL,
                "EMAIL");
        return parseFactor(value).orElse(MfaFactorType.EMAIL);
    }

    public boolean isMfaRequired(final Users user) {
        final Set<String> roleNames = user.getRole().stream()
                .map(role -> role.getName().toUpperCase(Locale.ROOT))
                .collect(Collectors.toSet());

        final Set<String> enforcedRoles = new LinkedHashSet<>(securityPolicyService.getCsvOrDefault(
                SecurityPolicyKeys.MFA_ENFORCED_ROLES,
                ""));
        final boolean enforcedByRole = enforcedRoles.stream()
                .map(value -> value.toUpperCase(Locale.ROOT))
                .anyMatch(roleNames::contains);

        if (enforcedByRole) {
            return true;
        }

        final boolean optionalPerUser = securityPolicyService.getBooleanOrDefault(
                SecurityPolicyKeys.MFA_OPTIONAL_PER_USER,
                true);
        if (!optionalPerUser) {
            return true;
        }

        return Boolean.TRUE.equals(user.getTwoFactorEnabled())
                || Boolean.TRUE.equals(user.getSmsMfaEnabled())
                || Boolean.TRUE.equals(user.getEmailMfaEnabled());
    }

    public Set<MfaFactorType> allowedFactorsForUser(final Users user) {
        String key = SecurityPolicyKeys.MFA_ALLOWED_FACTORS_USER;
        final Set<String> roles = user.getRole().stream()
                .map(role -> role.getName().toUpperCase(Locale.ROOT))
                .collect(java.util.stream.Collectors.toSet());
        if (roles.contains(UserRoles.SUPER_ADMIN.toUpperCase(Locale.ROOT))) {
            key = SecurityPolicyKeys.MFA_ALLOWED_FACTORS_SUPER_ADMIN;
        } else if (roles.contains(UserRoles.ADMIN.toUpperCase(Locale.ROOT))) {
            key = SecurityPolicyKeys.MFA_ALLOWED_FACTORS_ADMIN;
        }

        return parseFactors(securityPolicyService.getCsvOrDefault(key, "TOTP,SMS,EMAIL"));
    }

    private Set<MfaFactorType> parseFactors(final java.util.List<String> values) {
        final Set<MfaFactorType> factors = new LinkedHashSet<>();
        values.forEach(value -> parseFactor(value).ifPresent(factors::add));
        return factors;
    }

    private java.util.Optional<MfaFactorType> parseFactor(final String raw) {
        if (raw == null) {
            return java.util.Optional.empty();
        }
        return Arrays.stream(MfaFactorType.values())
                .filter(candidate -> candidate.name().equalsIgnoreCase(raw.trim()))
                .findFirst();
    }
}

