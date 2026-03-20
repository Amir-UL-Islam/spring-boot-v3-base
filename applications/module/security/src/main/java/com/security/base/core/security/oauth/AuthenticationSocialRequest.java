package com.security.base.core.security.oauth;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class AuthenticationSocialRequest {

    @NotNull
    private String code;

}
