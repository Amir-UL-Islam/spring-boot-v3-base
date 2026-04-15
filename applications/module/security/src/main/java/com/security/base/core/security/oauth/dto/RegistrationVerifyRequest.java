package com.security.base.core.security.oauth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistrationVerifyRequest {

    @NotBlank
    @Size(max = 128)
    private String challengeId;

    @NotBlank
    @Size(min = 4, max = 10)
    private String otp;
}

