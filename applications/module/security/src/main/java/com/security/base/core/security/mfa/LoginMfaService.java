package com.security.base.core.security.mfa;

import com.security.base.core.security.mfa.otp.OtpChallengeService;
import com.security.base.core.security.mfa.otp.OtpPurpose;
import com.security.base.core.security.mfa.otp.dto.OtpChallengeResponse;
import com.security.base.core.security.two_fa.TwoFactorService;
import com.security.base.core.users.model.entity.Users;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
@RequiredArgsConstructor
public class LoginMfaService {

    private final MfaPolicyResolver mfaPolicyResolver;
    private final OtpChallengeService otpChallengeService;
    private final TwoFactorService twoFactorService;

    public Optional<OtpChallengeResponse> enforceLoginMfa(final Users user,
                                                          final String otp,
                                                          final String challengeId,
                                                          final String otpChannelRaw) {
        if (!mfaPolicyResolver.isMfaRequired(user)) {
            return Optional.empty();
        }

        final Set<MfaFactorType> allowedFactors = mfaPolicyResolver.allowedFactorsForUser(user);
        final Set<MfaFactorType> enrolledFactors = resolveEnrolledFactors(user);

        final Set<MfaFactorType> effectiveFactors = enrolledFactors.stream()
                .filter(allowedFactors::contains)
                .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));

        if (effectiveFactors.isEmpty()) {
            throw new ResponseStatusException(UNAUTHORIZED, "MFA is required but no eligible factor is configured");
        }

        final MfaFactorType selectedFactor = chooseFactor(user, otpChannelRaw, effectiveFactors);
        if (selectedFactor == MfaFactorType.TOTP) {
            if (!twoFactorService.isCodeValid(user.getTotpSecret(), otp)) {
                throw new ResponseStatusException(UNAUTHORIZED, "Invalid or missing TOTP code");
            }
            return Optional.empty();
        }

        if (challengeId != null && !challengeId.isBlank()) {
            otpChallengeService.verifyChallenge(challengeId, user.getUsername(), OtpPurpose.LOGIN_MFA, otp);
            return Optional.empty();
        }

        if (otp != null && !otp.isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, "challengeId is required for SMS/EMAIL OTP validation");
        }

        final String destination = resolveDestination(user, selectedFactor);
        return Optional.of(otpChallengeService.issueChallenge(user, selectedFactor, OtpPurpose.LOGIN_MFA, destination));
    }

    private Set<MfaFactorType> resolveEnrolledFactors(final Users user) {
        final Set<MfaFactorType> factors = new java.util.LinkedHashSet<>();
        if (Boolean.TRUE.equals(user.getTwoFactorEnabled()) && user.getTotpSecret() != null && !user.getTotpSecret().isBlank()) {
            factors.add(MfaFactorType.TOTP);
        }
        if (Boolean.TRUE.equals(user.getSmsMfaEnabled()) && Boolean.TRUE.equals(user.getPhoneVerified())) {
            factors.add(MfaFactorType.SMS);
        }
        if (Boolean.TRUE.equals(user.getEmailMfaEnabled()) && Boolean.TRUE.equals(user.getEmailVerified())) {
            factors.add(MfaFactorType.EMAIL);
        }
        return factors;
    }

    private MfaFactorType chooseFactor(final Users user,
                                       final String raw,
                                       final Set<MfaFactorType> factors) {
        if (raw != null && !raw.isBlank()) {
            final MfaFactorType fromRequest = parseFactor(raw)
                    .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "Unsupported otpChannel value"));
            if (!factors.contains(fromRequest)) {
                throw new ResponseStatusException(UNAUTHORIZED, "Requested OTP channel is not enabled for this user");
            }
            return fromRequest;
        }

        final MfaFactorType preferred = parseFactor(user.getPreferredMfaFactor()).orElse(null);
        if (preferred != null && factors.contains(preferred)) {
            return preferred;
        }

        if (factors.contains(MfaFactorType.TOTP)) {
            return MfaFactorType.TOTP;
        }
        if (factors.contains(MfaFactorType.EMAIL)) {
            return MfaFactorType.EMAIL;
        }
        return MfaFactorType.SMS;
    }

    private String resolveDestination(final Users user, final MfaFactorType factor) {
        return switch (factor) {
            case SMS -> user.getPhone();
            case EMAIL -> user.getEmail();
            default -> throw new ResponseStatusException(BAD_REQUEST, "Destination is not applicable for TOTP");
        };
    }

    private Optional<MfaFactorType> parseFactor(final String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(MfaFactorType.valueOf(value.trim().toUpperCase(Locale.ROOT)));
        } catch (final IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}

