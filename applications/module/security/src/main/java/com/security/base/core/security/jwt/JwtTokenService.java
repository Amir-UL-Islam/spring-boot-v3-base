package com.security.base.core.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

import com.security.base.core.role.model.entity.Role;
import com.security.base.core.users.model.entity.Users;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class JwtTokenService {

    private static final Duration ACCESS_TOKEN_VALIDITY = Duration.ofMinutes(60);
    private static final Duration MAX_ACCESS_TOKEN_VALIDITY = Duration.ofMinutes(60);
    private static final Duration REFRESH_TOKEN_VALIDITY = Duration.ofHours(30);
    private static final Duration MAX_REFRESH_TOKEN_VALIDITY = Duration.ofHours(30);

    private final Algorithm rsa256;
    private final JWTVerifier verifier;

    public JwtTokenService(
            @Value("classpath:certs/public.pem") final RSAPublicKey publicKey,
            @Value("classpath:certs/private.pem") final RSAPrivateKey privateKey
    ) {
        this.rsa256 = Algorithm.RSA256(publicKey, privateKey);
        this.verifier = JWT.require(this.rsa256).build();
    }

    public String generateAccessToken(
            final Users userDetails,
            final String loginType,
            final Duration validity
    ) {
        return generateToken(userDetails, loginType, TokenType.ACCESS, validity,
                ACCESS_TOKEN_VALIDITY, MAX_ACCESS_TOKEN_VALIDITY);
    }

    public String generateRefreshToken(
            final Users userDetails,
            final String loginType,
            final Duration validity
    ) {
        return generateToken(userDetails, loginType, TokenType.REFRESH, validity,
                REFRESH_TOKEN_VALIDITY, MAX_REFRESH_TOKEN_VALIDITY);
    }

    private String generateToken(
            final Users userDetails,
            final String loginType,
            final TokenType tokenType,
            final Duration validity,
            final Duration defaultValidity,
            final Duration maxValidity
    ) {
        final Instant now = Instant.now();
        final Duration tokenValidity = validity == null ? defaultValidity
                : (validity.compareTo(maxValidity) > 0 ? maxValidity : validity);
        return JWT.create()
                .withSubject(userDetails.getUsername())
                .withClaim("login_type", loginType)
                .withClaim("token_type", tokenType.getValue())
                .withClaim("token_version", userDetails.getTokenVersion())
                .withArrayClaim("authorities", userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .filter(Objects::nonNull)
                        .filter(authority -> !authority.startsWith("ROLE_"))
                        .distinct()
                        .toArray(String[]::new))
                .withArrayClaim("roles", userDetails.getRole().stream()
                        .map(Role::getName)
                        .distinct()
                        .toArray(String[]::new))
                .withIssuer("app")
                .withIssuedAt(now)
                .withExpiresAt(now.plus(tokenValidity))
                .sign(this.rsa256);
    }

    public DecodedJWT validateAccessToken(final String token) {
        return validateToken(token, TokenType.ACCESS);
    }

    public DecodedJWT validateRefreshToken(final String token) {
        return validateToken(token, TokenType.REFRESH);
    }

    public long accessTokenValiditySeconds() {
        return ACCESS_TOKEN_VALIDITY.getSeconds();
    }

    private DecodedJWT validateToken(final String token, final TokenType expectedType) {
        try {
            final DecodedJWT jwt = verifier.verify(token);
            final String tokenType = jwt.getClaim("token_type").asString();
            if (!expectedType.getValue().equals(tokenType)) {
                log.warn("Invalid token type: Expected {}, Found: {}", expectedType.getValue(),
                        tokenType);
                return null;
            }
            return jwt;
        } catch (final JWTVerificationException verificationEx) {
            log.warn("token invalid: {}", verificationEx.getMessage());
            return null;
        }
    }

    @Getter
    private enum TokenType {
        ACCESS("access"),
        REFRESH("refresh");

        private final String value;

        TokenType(final String value) {
            this.value = value;
        }

    }

}
