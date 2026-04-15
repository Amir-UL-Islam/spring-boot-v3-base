package com.security.base.core.security.oauth.service;

import com.security.base.core.security.mfa.MfaFactorType;
import com.security.base.core.security.mfa.MfaPolicyResolver;
import com.security.base.core.security.mfa.otp.OtpChallenge;
import com.security.base.core.security.mfa.otp.OtpChallengeRepository;
import com.security.base.core.security.mfa.otp.OtpChallengeService;
import com.security.base.core.security.mfa.otp.OtpPurpose;
import com.security.base.core.security.mfa.otp.dto.OtpChallengeResponse;
import com.security.base.core.role.repository.RoleRepository;
import com.security.base.core.security.oauth.UserRoles;
import com.security.base.core.security.oauth.dto.RegistrationInitRequest;
import com.security.base.core.security.oauth.dto.RegistrationInitResponse;
import com.security.base.core.security.oauth.dto.RegistrationVerifyRequest;
import com.security.base.core.users.model.entity.Users;
import com.security.base.core.users.repository.UsersRepository;

import java.util.Set;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;


@Service
@Slf4j
@RequiredArgsConstructor
public class RegistrationService {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final MfaPolicyResolver mfaPolicyResolver;
    private final OtpChallengeService otpChallengeService;
    private final OtpChallengeRepository otpChallengeRepository;

    @Transactional
    public RegistrationInitResponse initRegistration(final RegistrationInitRequest request) {
        final MfaFactorType selectedChannel = resolveRegistrationChannel(request.getVerificationChannel());
        final String destination = resolveDestination(request, selectedChannel);

        log.info("starting registration for user: {} using channel {}", request.getUsername(), selectedChannel);

        Users users = usersRepository.findByUsernameIgnoreCase(request.getUsername());
        if (users != null && Boolean.TRUE.equals(users.getAccountEnabled())) {
            throw new ResponseStatusException(BAD_REQUEST, "Username already registered");
        }
        if (users == null) {
            users = new Users();
            users.setUsername(request.getUsername());
        }

        users.setName(request.getName());
        users.setEmail(request.getEmail());
        users.setPhone(request.getPhone());
        users.setPassword(passwordEncoder.encode(request.getPassword()));
        users.setAccountEnabled(false);
        users.setEmailVerified(false);
        users.setPhoneVerified(false);
        users.setTwoFactorEnabled(false);
        users.setSmsMfaEnabled(false);
        users.setEmailMfaEnabled(false);

        // Assign least-privileged default role for new accounts.
        final var userRole = roleRepository.findByName(UserRoles.USER);
        if (userRole == null) {
            throw new IllegalStateException("USER role missing. Check RoleLoader initialization order.");
        }
        users.setRole(Set.of(userRole));
        users.setTokenVersion(1);
        usersRepository.save(users);

        final OtpChallengeResponse challenge = otpChallengeService.issueChallenge(
                users,
                selectedChannel,
                OtpPurpose.REGISTRATION,
                destination
        );

        final RegistrationInitResponse response = new RegistrationInitResponse();
        response.setChallengeId(challenge.getChallengeId());
        response.setChannel(challenge.getFactor());
        response.setMaskedDestination(challenge.getMaskedDestination());
        response.setExpiresInSeconds(challenge.getExpiresInSeconds());
        response.setMessage("Registration OTP sent");
        return response;
    }

    @Transactional
    public void verifyRegistration(final RegistrationVerifyRequest request) {
        final OtpChallenge challenge = otpChallengeRepository.findByChallengeId(request.getChallengeId())
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Registration challenge not found"));

        otpChallengeService.verifyChallenge(
                request.getChallengeId(),
                challenge.getUsername(),
                OtpPurpose.REGISTRATION,
                request.getOtp()
        );

        final Users users = usersRepository.findByUsernameIgnoreCase(challenge.getUsername());
        if (users == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "User not found for challenge");
        }

        users.setAccountEnabled(true);
        if (challenge.getFactor() == MfaFactorType.EMAIL) {
            users.setEmailVerified(true);
        }
        if (challenge.getFactor() == MfaFactorType.SMS) {
            users.setPhoneVerified(true);
        }
        usersRepository.save(users);
    }

    private MfaFactorType resolveRegistrationChannel(final MfaFactorType requested) {
        final Set<MfaFactorType> allowed = mfaPolicyResolver.allowedRegistrationChannels();
        final MfaFactorType selected = requested != null ? requested : mfaPolicyResolver.defaultRegistrationChannel();
        if (selected == MfaFactorType.TOTP || !allowed.contains(selected)) {
            throw new ResponseStatusException(BAD_REQUEST, "Registration verification channel is not allowed by policy");
        }
        return selected;
    }

    private String resolveDestination(final RegistrationInitRequest request, final MfaFactorType channel) {
        return switch (channel) {
            case EMAIL -> {
                if (request.getEmail() == null || request.getEmail().isBlank()) {
                    throw new ResponseStatusException(BAD_REQUEST, "Email is required for EMAIL registration verification");
                }
                yield request.getEmail();
            }
            case SMS -> {
                if (request.getPhone() == null || request.getPhone().isBlank()) {
                    throw new ResponseStatusException(BAD_REQUEST, "Phone is required for SMS registration verification");
                }
                yield request.getPhone();
            }
            default -> throw new ResponseStatusException(BAD_REQUEST, "Unsupported registration verification channel");
        };
    }

}
