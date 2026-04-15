package com.security.base.core.security.mfa.otp;

import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OtpChallengeRepository extends JpaRepository<OtpChallenge, Long> {

    Optional<OtpChallenge> findByChallengeId(String challengeId);

    void deleteByExpiresAtBefore(Instant cutoff);
}

