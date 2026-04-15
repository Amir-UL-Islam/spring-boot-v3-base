package com.security.base.core.security.oauth.dto;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class AuthenticationResponse {

    private String accessToken;
    private String refreshToken;
    private boolean requiresMfa;
    private String mfaChallengeId;
    private String mfaChannel;
    private String mfaMessage;
    private long mfaExpiresInSeconds;

}
