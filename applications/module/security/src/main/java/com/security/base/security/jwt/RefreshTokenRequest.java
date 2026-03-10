package com.security.base.security.jwt;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class RefreshTokenRequest {

    @NotNull
    private String refreshToken;

}
