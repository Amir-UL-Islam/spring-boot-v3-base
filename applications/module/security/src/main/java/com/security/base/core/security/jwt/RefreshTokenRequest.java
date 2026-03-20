package com.security.base.core.security.jwt;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class RefreshTokenRequest {

    @NotNull
    private String refreshToken;

}
