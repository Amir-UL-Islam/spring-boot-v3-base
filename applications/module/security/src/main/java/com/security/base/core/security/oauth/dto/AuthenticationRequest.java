package com.security.base.core.security.oauth.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class AuthenticationRequest {

    @NotNull
    @Size(max = 255)
    private String username;

    @NotNull
    @Size(max = 72)
    private String password;

    // Optional TOTP code when two-factor is enabled
    private String otp;

    // Optional MFA channel hint: TOTP, SMS, EMAIL
    private String otpChannel;

    // Required for SMS/EMAIL challenge verification in login second step
    private String challengeId;

}
