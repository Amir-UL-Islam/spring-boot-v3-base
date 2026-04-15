package com.security.base.core.security.policy.config;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SecurityPolicySettingRepository extends JpaRepository<SecurityPolicySetting, Long> {

    Optional<SecurityPolicySetting> findByPolicyKeyIgnoreCase(String policyKey);
}

