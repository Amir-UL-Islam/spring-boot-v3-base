package com.security.base.core.security.policy.config.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SecurityPolicySettingDTO {

    @NotBlank
    @Size(max = 255)
    private String key;

    @NotBlank
    @Size(max = 4000)
    private String value;
}

