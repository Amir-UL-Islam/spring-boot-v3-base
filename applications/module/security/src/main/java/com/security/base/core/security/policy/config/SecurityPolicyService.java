package com.security.base.core.security.policy.config;

import com.security.base.core.security.policy.config.dto.SecurityPolicySettingDTO;
import jakarta.transaction.Transactional;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class SecurityPolicyService {

    private final SecurityPolicySettingRepository repository;

    public Map<String, String> getPolicyMap() {
        return repository.findAll().stream()
                .collect(Collectors.toMap(SecurityPolicySetting::getPolicyKey,
                        SecurityPolicySetting::getPolicyValue,
                        (first, second) -> second,
                        LinkedHashMap::new));
    }

    public String getValueOrDefault(final String key, final String defaultValue) {
        return repository.findByPolicyKeyIgnoreCase(key)
                .map(SecurityPolicySetting::getPolicyValue)
                .orElse(defaultValue);
    }

    public boolean getBooleanOrDefault(final String key, final boolean defaultValue) {
        return Boolean.parseBoolean(getValueOrDefault(key, Boolean.toString(defaultValue)));
    }

    public List<String> getCsvOrDefault(final String key, final String defaultValue) {
        final String value = getValueOrDefault(key, defaultValue);
        return List.of(value.split(","))
                .stream()
                .map(String::trim)
                .filter(part -> !part.isEmpty())
                .toList();
    }

    public void upsert(final String key, final String value) {
        final Optional<SecurityPolicySetting> existing = repository.findByPolicyKeyIgnoreCase(key);
        final SecurityPolicySetting policySetting = existing.orElseGet(SecurityPolicySetting::new);
        policySetting.setPolicyKey(key);
        policySetting.setPolicyValue(value);
        repository.save(policySetting);
    }

    public void upsertAll(final List<SecurityPolicySettingDTO> settings) {
        settings.forEach(setting -> upsert(setting.getKey(), setting.getValue()));
    }
}

