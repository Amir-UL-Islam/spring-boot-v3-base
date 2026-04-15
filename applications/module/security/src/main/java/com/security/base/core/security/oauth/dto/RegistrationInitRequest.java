package com.security.base.core.security.oauth.dto;

import com.security.base.core.security.mfa.MfaFactorType;
import com.security.base.core.users.UsersUsernameUnique;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistrationInitRequest {

    @Size(max = 255)
    private String name;

    @Size(max = 255)
    private String email;

    @Size(max = 32)
    private String phone;

    @NotNull
    @Size(max = 255)
    @UsersUsernameUnique(message = "{registration.register.taken}")
    private String username;

    @NotNull
    @Size(max = 72)
    private String password;

    @NotNull
    private MfaFactorType verificationChannel;
}

