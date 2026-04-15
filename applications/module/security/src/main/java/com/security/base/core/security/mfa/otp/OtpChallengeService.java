package com.security.base.core.security.mfa.otp;

import com.security.base.core.security.mfa.MfaFactorType;
import com.security.base.core.security.mfa.otp.config.OtpProperties;
import com.security.base.core.security.mfa.otp.dto.OtpChallengeResponse;
import com.security.base.core.security.mfa.otp.dto.OtpVerificationResult;
import com.security.base.core.users.model.entity.Users;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
@RequiredArgsConstructor
@Transactional
public class OtpChallengeService {

    private final OtpChallengeRepository repository;
    private final OtpDeliveryService otpDeliveryService;
    private final PasswordEncoder passwordEncoder;
    private final OtpProperties otpProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    public OtpChallengeResponse issueChallenge(final Users user,
                                               final MfaFactorType factor,
                                               final OtpPurpose purpose,
                                               final String destination) {
        validateSupportedFactor(factor);
        if (destination == null || destination.isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, "Missing destination for OTP factor " + factor);
        }

        repository.deleteByExpiresAtBefore(Instant.now());

        final String code = generateCode();
        final Instant now = Instant.now();

        final OtpChallenge challenge = new OtpChallenge();
        challenge.setChallengeId(UUID.randomUUID().toString());
        challenge.setUsername(user.getUsername());
        challenge.setDestination(destination);
        challenge.setFactor(factor);
        challenge.setPurpose(purpose);
        challenge.setCodeHash(passwordEncoder.encode(code));
        challenge.setExpiresAt(now.plusSeconds(otpProperties.getTtlSeconds()));
        challenge.setAttempts(0);
        challenge.setMaxAttempts(otpProperties.getMaxAttempts());
        challenge.setLastSentAt(now);
        repository.save(challenge);

        otpDeliveryService.sendOtp(factor, destination, buildMessage(code));

        return OtpChallengeResponse.builder()
                .challengeId(challenge.getChallengeId())
                .factor(factor)
                .maskedDestination(mask(destination, factor))
                .expiresInSeconds(otpProperties.getTtlSeconds())
                .message("OTP sent")
                .build();
    }

    public OtpVerificationResult verifyChallenge(final String challengeId,
                                                 final String username,
                                                 final OtpPurpose purpose,
                                                 final String code) {
        final OtpChallenge challenge = repository.findByChallengeId(challengeId)
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "OTP challenge not found"));

        if (!challenge.getUsername().equalsIgnoreCase(username)) {
            throw new ResponseStatusException(UNAUTHORIZED, "OTP challenge does not belong to this user");
        }
        if (challenge.getPurpose() != purpose) {
            throw new ResponseStatusException(UNAUTHORIZED, "OTP challenge purpose mismatch");
        }
        if (challenge.getConsumedAt() != null) {
            throw new ResponseStatusException(UNAUTHORIZED, "OTP challenge already consumed");
        }
        if (challenge.getExpiresAt().isBefore(Instant.now())) {
            throw new ResponseStatusException(UNAUTHORIZED, "OTP challenge expired");
        }
        if (challenge.getAttempts() >= challenge.getMaxAttempts()) {
            throw new ResponseStatusException(TOO_MANY_REQUESTS, "OTP attempts exceeded");
        }

        challenge.setAttempts(challenge.getAttempts() + 1);

        if (code == null || code.isBlank() || !passwordEncoder.matches(code, challenge.getCodeHash())) {
            repository.save(challenge);
            throw new ResponseStatusException(UNAUTHORIZED, "Invalid OTP code");
        }

        challenge.setConsumedAt(Instant.now());
        repository.save(challenge);

        return OtpVerificationResult.builder()
                .verified(true)
                .factor(challenge.getFactor())
                .username(challenge.getUsername())
                .message("OTP verified")
                .build();
    }

    private void validateSupportedFactor(final MfaFactorType factor) {
        if (factor != MfaFactorType.SMS && factor != MfaFactorType.EMAIL) {
            throw new ResponseStatusException(BAD_REQUEST, "OTP challenge supports only SMS or EMAIL");
        }
    }

    private String generateCode() {
        final int length = Math.max(4, otpProperties.getCodeLength());
        final int lowerBound = (int) Math.pow(10, length - 1);
        final int upperBound = (int) Math.pow(10, length);
        final int value = secureRandom.nextInt(upperBound - lowerBound) + lowerBound;
        return Integer.toString(value);
    }

    private String buildMessage(final String code) {
        return "Your verification code is " + code + ". It expires in " + otpProperties.getTtlSeconds() + " seconds.";
    }

    private String mask(final String destination, final MfaFactorType factor) {
        if (destination.length() < 4) {
            return "***";
        }
        if (factor == MfaFactorType.EMAIL) {
            final int at = destination.indexOf('@');
            if (at <= 1) {
                return "***";
            }
            return destination.substring(0, 1) + "***" + destination.substring(at - 1);
        }
        return "***" + destination.substring(destination.length() - 4);
    }
}

