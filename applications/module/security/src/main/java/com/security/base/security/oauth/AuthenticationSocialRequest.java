package com.security.base.security.oauth;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class AuthenticationSocialRequest {

    @NotNull
    private String code;

}
