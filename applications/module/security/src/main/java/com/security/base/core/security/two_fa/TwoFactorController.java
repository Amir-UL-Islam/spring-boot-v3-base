package com.security.base.core.security.two_fa;

import com.security.base.core.users.model.entity.Users;
import com.security.base.core.users.repository.UsersRepository;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/2fa")
@PreAuthorize("isAuthenticated()")
@Tag(name = "Two-Factor Authentication", description = "APIs for managing two-factor authentication setup and activation")
public class TwoFactorController {

    private final UsersRepository usersRepository;
    private final TwoFactorService twoFactorService;

    public TwoFactorController(final UsersRepository usersRepository,
                               final TwoFactorService twoFactorService) {
        this.usersRepository = usersRepository;
        this.twoFactorService = twoFactorService;
    }

    @PostMapping("/setup")
    public ResponseEntity<Map<String, String>> setup(final Authentication authentication) {
        final Users user = usersRepository.findByUsernameIgnoreCase(authentication.getName());
        final var credentials = twoFactorService.generateSecret();
        user.setTotpSecret(credentials.getKey());
        user.setTwoFactorEnabled(false);
        usersRepository.save(user);
        final String otpAuthUrl = buildOtpAuthUrl(user.getUsername(), credentials.getKey());
        return ResponseEntity.ok(Map.of(
                "secret", credentials.getKey(),
                "otpauth_url", otpAuthUrl));
    }

    @PostMapping("/activate")
    public ResponseEntity<Void> activate(@RequestBody final Map<String, String> payload,
                                         final Authentication authentication) {
        final Users user = usersRepository.findByUsernameIgnoreCase(authentication.getName());
        final String code = payload.get("code");
        if (!twoFactorService.isCodeValid(user.getTotpSecret(), code)) {
            return ResponseEntity.status(401).build();
        }
        user.setTwoFactorEnabled(true);
        user.setPreferredMfaFactor("TOTP");
        usersRepository.save(user);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/disable")
    public ResponseEntity<Void> disable(final Authentication authentication) {
        final Users user = usersRepository.findByUsernameIgnoreCase(authentication.getName());
        user.setTwoFactorEnabled(false);
        user.setTotpSecret(null);
        if ("TOTP".equalsIgnoreCase(user.getPreferredMfaFactor())) {
            user.setPreferredMfaFactor(null);
        }
        usersRepository.save(user);
        return ResponseEntity.ok().build();
    }

    private String buildOtpAuthUrl(final String username, final String secret) {
        final String issuer = URLEncoder.encode("stateless-security", StandardCharsets.UTF_8);
        final String account = URLEncoder.encode(username, StandardCharsets.UTF_8);
        return String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s", issuer, account, secret, issuer);
    }
}

