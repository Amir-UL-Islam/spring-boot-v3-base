package com.security.base.core.security.mfa.otp;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import com.security.base.core.security.mfa.MfaFactorType;
import com.security.base.core.security.mfa.otp.config.OtpProperties;
import com.security.base.core.security.mfa.otp.dto.OtpChallengeResponse;
import com.security.base.core.security.mfa.otp.provider.OtpDeliveryProvider;
import com.security.base.core.users.model.entity.Users;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class OtpChallengeServiceTest {

    @Mock
    private OtpChallengeRepository repository;

    @Mock
    private OtpDeliveryProvider deliveryProvider;

    private OtpChallengeService service;

    @BeforeEach
    void setUp() {
        when(deliveryProvider.factor()).thenReturn(MfaFactorType.SMS);
        doNothing().when(deliveryProvider).sendOtp(any(), any());

        final OtpProperties otpProperties = new OtpProperties();
        otpProperties.setTtlSeconds(300);
        otpProperties.setCodeLength(6);
        otpProperties.setMaxAttempts(3);

        final OtpDeliveryService otpDeliveryService = new OtpDeliveryService(java.util.List.of(deliveryProvider));
        service = new OtpChallengeService(repository, otpDeliveryService,
                PasswordEncoderFactories.createDelegatingPasswordEncoder(), otpProperties);
    }

    @Test
    void shouldIssueAndVerifySmsChallenge() {
        final Users user = new Users();
        user.setUsername("user1");
        user.setPhone("01700000000");

        final AtomicReference<OtpChallenge> saved = new AtomicReference<>();
        when(repository.save(any(OtpChallenge.class))).thenAnswer(invocation -> {
            final OtpChallenge challenge = invocation.getArgument(0);
            saved.set(challenge);
            return challenge;
        });
        when(repository.findByChallengeId(any())).thenAnswer(invocation -> Optional.ofNullable(saved.get()));
        doNothing().when(repository).deleteByExpiresAtBefore(any(Instant.class));

        final OtpChallengeResponse response = service.issueChallenge(user, MfaFactorType.SMS, OtpPurpose.LOGIN_MFA, user.getPhone());
        assertNotNull(response.getChallengeId());

        assertThrows(ResponseStatusException.class, () -> service.verifyChallenge(
                response.getChallengeId(), user.getUsername(), OtpPurpose.LOGIN_MFA, "000000"
        ));
    }
}

