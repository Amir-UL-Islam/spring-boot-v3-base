package com.security.base.core.security.mfa.otp;

import com.hmtmcse.module.entity.BaseEntity;
import com.problemfighter.pfspring.restapi.inter.model.RestEntity;
import com.security.base.core.security.mfa.MfaFactorType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.Instant;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
public class OtpChallenge extends BaseEntity implements RestEntity {

    @Column(nullable = false, unique = true)
    private String challengeId;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String destination;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MfaFactorType factor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OtpPurpose purpose;

    @Column(nullable = false, length = 255)
    private String codeHash;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column
    private Instant consumedAt;

    @Column(nullable = false)
    private int attempts;

    @Column(nullable = false)
    private int maxAttempts;

    @Column
    private Instant lastSentAt;
}

