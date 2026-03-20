package com.security.base.core.security.oauth;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class AuthenticationResponse {

    private String accessToken;
    private String refreshToken;

}
