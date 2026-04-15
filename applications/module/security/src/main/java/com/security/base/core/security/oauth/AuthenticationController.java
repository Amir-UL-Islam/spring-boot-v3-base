package com.security.base.core.security.oauth;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.security.base.core.role.repository.RoleRepository;
import com.security.base.core.security.jwt.CustomUserDetailsService;
import com.security.base.core.security.jwt.JwtTokenService;
import com.security.base.core.security.jwt.RefreshTokenRequest;
import com.security.base.core.security.two_fa.TwoFactorService;
import com.security.base.core.users.model.entity.Users;
import com.security.base.core.users.repository.UsersRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;


@RestController
@Slf4j
@Tag(name = "Authentication", description = "APIs for user authentication and token management")
public class AuthenticationController {

    private final AuthenticationProvider authenticationProvider;
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtTokenService jwtTokenService;
    private final Environment environment;
    private final UsersRepository usersRepository;
    private final String baseHost;
    private final RoleRepository roleRepository;
    private final TwoFactorService twoFactorService;
    private final RestClient googleClient = RestClient.builder()
            .baseUrl("https://oauth2.googleapis.com/")
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .build();

    public AuthenticationController(
            final AuthenticationProvider authenticationProvider,
            final CustomUserDetailsService customUserDetailsService,
            final JwtTokenService jwtTokenService, final Environment environment,
            final UsersRepository usersRepository,
            @Value("${app.baseHost}") final String baseHost,
            final RoleRepository roleRepository,
            final TwoFactorService twoFactorService
    ) {
        this.authenticationProvider = authenticationProvider;
        this.customUserDetailsService = customUserDetailsService;
        this.jwtTokenService = jwtTokenService;
        this.environment = environment;
        this.usersRepository = usersRepository;
        this.baseHost = baseHost;
        this.roleRepository = roleRepository;
        this.twoFactorService = twoFactorService;
    }

    @PostMapping("/authenticate")
    public AuthenticationResponse authenticate(
            @RequestBody @Valid final AuthenticationRequest authenticationRequest) {
        try {
            authenticationProvider.authenticate(new UsernamePasswordAuthenticationToken(
                    authenticationRequest.getUsername(), authenticationRequest.getPassword()));
        } catch (final BadCredentialsException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        final Users userDetails = customUserDetailsService.loadUserByUsername(authenticationRequest.getUsername());
        enforceTwoFactor(authenticationRequest.getOtp(), userDetails.getUsername());
        return buildAuthenticationResponse(userDetails, "direct", null, null);
    }

    @Transactional
    @PostMapping(value = "/oauth/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> passwordGrant(
            @RequestParam("username") final String username,
            @RequestParam("password") final String password,
            @RequestParam(value = "scope", required = false) final String scope,
            @RequestParam(value = "otp", required = false) final String otp) {
        try {
            authenticationProvider.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (final BadCredentialsException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        final Users userDetails = customUserDetailsService.loadUserByUsername(username);
        enforceTwoFactor(otp, userDetails.getUsername());
        final String accessToken = jwtTokenService.generateAccessToken(userDetails, "direct", null);
        final String refreshToken = jwtTokenService.generateRefreshToken(userDetails, "direct", null);

        final String resolvedScope = scope != null ? scope
                : String.join(" ", userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).toList());

        return Map.of(
                "access_token", accessToken,
                "refresh_token", refreshToken,
                "token_type", "bearer",
                "expires_in", jwtTokenService.accessTokenValiditySeconds(),
                "scope", resolvedScope
        );
    }

    private AuthenticationResponse synchronizeUserAndGetToken(final String loginType,
                                                              final String subject, final Map<String, Object> tokeninfoResponse, final Instant expiresAt) {
        Users users = usersRepository.findByEmail(subject);
        if (users == null) {
            log.info("adding new user after successful authentication: {}", subject);
            final var userRole = roleRepository.findByName(UserRoles.USER);
            if (userRole == null) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "USER role missing");
            }
            users = new Users();
            users.setEmail(subject);
            users.setUsername(subject);
            users.setName(tokeninfoResponse.get("name").toString());
            // assign least-privileged default role for social login onboarding
            users.setRole(Set.of(userRole));
            users.setTokenVersion(1);
        } else {
            log.info("updating existing user after successful authentication: {}", subject);
        }
        usersRepository.save(users);

        final Users userDetails = customUserDetailsService.loadUserByUsername(subject);
        final Duration validity = Duration.between(Instant.now(), expiresAt);
        return buildAuthenticationResponse(userDetails, loginType, validity, null);
    }

    @PostMapping("/refresh-token")
    public AuthenticationResponse refreshToken(@RequestBody @Valid final RefreshTokenRequest refreshTokenRequest) {
        final DecodedJWT refreshTokenJwt = jwtTokenService.validateRefreshToken(refreshTokenRequest.getRefreshToken());
        if (refreshTokenJwt == null || refreshTokenJwt.getSubject() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        final String loginType = refreshTokenJwt.getClaim("login_type").asString();
        if (loginType == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        final Users userDetails;

        try {
            userDetails = customUserDetailsService.loadUserByUsername(refreshTokenJwt.getSubject());
        } catch (final UsernameNotFoundException userNotFoundEx) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        return buildAuthenticationResponse(userDetails, loginType, null, null);
    }

    private AuthenticationResponse buildAuthenticationResponse(
            final Users userDetails,
            final String loginType,
            final Duration accessTokenValidity,
            final Duration refreshTokenValidity
    ) {
        final AuthenticationResponse authenticationResponse = new AuthenticationResponse();
        authenticationResponse.setAccessToken(
                jwtTokenService.generateAccessToken(userDetails, loginType, accessTokenValidity));
        authenticationResponse.setRefreshToken(
                jwtTokenService.generateRefreshToken(userDetails, loginType, refreshTokenValidity));
        return authenticationResponse;
    }

    @PostMapping("/authenticateGoogle")
    public AuthenticationResponse authenticateGoogle(
            @RequestBody @Valid final AuthenticationSocialRequest authenticationSocialRequest) {
        log.info("exchanging google code");
        final String providerId = "google";
        final String clientId = environment.getProperty("app." + providerId + ".client-id");
        final String clientSecret = environment.getProperty("app." + providerId + ".client-secret");
        final RestClient.ResponseSpec accessTokenSpec = googleClient.post()
                .uri("token")
                .body(Map.of("client_id", clientId, "client_secret", clientSecret,
                        "redirect_uri", baseHost + "/completeLogin?provider=google", "grant_type", "authorization_code",
                        "code", authenticationSocialRequest.getCode()))
                .retrieve();
        final Map<String, Object> accessTokenResponse = accessTokenSpec.body(new ParameterizedTypeReference<>() {
        });

        log.info("validating google access token");
        final RestClient.ResponseSpec tokenInfoSpec = googleClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("tokeninfo")
                        .queryParam("id_token", accessTokenResponse.get("id_token"))
                        .build())
                .retrieve();
        final Map<String, Object> tokenInfoResponse = tokenInfoSpec.body(new ParameterizedTypeReference<>() {
        });
        if (!clientId.equals(tokenInfoResponse.get("aud"))) {
            log.warn("google app id not matching");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        final String subject = tokenInfoResponse.get("sub").toString();
        final Instant expiresAt = Instant.ofEpochSecond(Long.parseLong(tokenInfoResponse.get("exp").toString()));
        if (expiresAt.isBefore(Instant.now())) {
            log.warn("google token has expired");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return synchronizeUserAndGetToken(providerId, subject, tokenInfoResponse, expiresAt);
    }

    private void enforceTwoFactor(final String otp, final String username) {
        final Users users = usersRepository.findByUsernameIgnoreCase(username);
        if (Boolean.TRUE.equals(users.getTwoFactorEnabled())) {
            if (!twoFactorService.isCodeValid(users.getTotpSecret(), otp)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing 2FA code");
            }
        }
    }

}
