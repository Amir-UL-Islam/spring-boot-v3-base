package com.security.base.core.security.mfa;

import com.security.base.core.security.mfa.dto.MfaSettingsRequest;
import com.security.base.core.security.mfa.dto.MfaSettingsResponse;
import com.security.base.core.users.model.entity.Users;
import com.security.base.core.users.repository.UsersRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequestMapping("/api/mfa/settings")
@RequiredArgsConstructor
@Tag(name = "MFA Settings", description = "Manage authenticated user's MFA channel preferences")
public class MfaSettingsController {

    private final UsersRepository usersRepository;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MfaSettingsResponse> get(final Authentication authentication) {
        final Users user = usersRepository.findByUsernameIgnoreCase(authentication.getName());
        if (user == null) {
            throw new ResponseStatusException(UNAUTHORIZED);
        }

        return ResponseEntity.ok(MfaSettingsResponse.builder()
                .totpEnabled(Boolean.TRUE.equals(user.getTwoFactorEnabled()))
                .smsEnabled(Boolean.TRUE.equals(user.getSmsMfaEnabled()))
                .emailEnabled(Boolean.TRUE.equals(user.getEmailMfaEnabled()))
                .phoneVerified(Boolean.TRUE.equals(user.getPhoneVerified()))
                .emailVerified(Boolean.TRUE.equals(user.getEmailVerified()))
                .preferredFactor(user.getPreferredMfaFactor())
                .build());
    }

    @PutMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> update(final Authentication authentication,
                                       @RequestBody final MfaSettingsRequest request) {
        final Users user = usersRepository.findByUsernameIgnoreCase(authentication.getName());
        if (user == null) {
            throw new ResponseStatusException(UNAUTHORIZED);
        }

        if (Boolean.TRUE.equals(request.getSmsEnabled()) && !Boolean.TRUE.equals(user.getPhoneVerified())) {
            throw new ResponseStatusException(BAD_REQUEST, "Phone must be verified before enabling SMS MFA");
        }
        if (Boolean.TRUE.equals(request.getEmailEnabled()) && !Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new ResponseStatusException(BAD_REQUEST, "Email must be verified before enabling EMAIL MFA");
        }

        if (request.getSmsEnabled() != null) {
            user.setSmsMfaEnabled(request.getSmsEnabled());
        }
        if (request.getEmailEnabled() != null) {
            user.setEmailMfaEnabled(request.getEmailEnabled());
        }
        if (request.getPreferredFactor() != null) {
            user.setPreferredMfaFactor(request.getPreferredFactor().name());
        }

        usersRepository.save(user);
        return ResponseEntity.noContent().build();
    }
}

