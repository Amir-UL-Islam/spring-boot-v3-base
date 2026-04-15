package com.security.base.core.security.policy.config;

import com.hmtmcse.module.entity.BaseEntity;
import com.problemfighter.pfspring.restapi.inter.model.RestEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
public class SecurityPolicySetting extends BaseEntity implements RestEntity {

    @Column(nullable = false, unique = true)
    private String policyKey;

    @Column(nullable = false, length = 4000)
    private String policyValue;
}

