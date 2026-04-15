package com.security.base.core.security.oauth.controller;

import com.security.base.core.users.model.entity.Users;
import com.security.base.core.users.repository.UsersRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/me")
@Tag(name = "Current User", description = "Current authenticated user permissions and matrix data")
public class CurrentUserPermissionController {

    private final UsersRepository usersRepository;

    public CurrentUserPermissionController(final UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    @GetMapping("/permissions")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> myPermissions(final Authentication authentication) {
        final Users user = usersRepository.findByUsernameIgnoreCase(authentication.getName());
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        final List<String> roleNames = user.getRole().stream().map(com.security.base.core.role.model.entity.Role::getName).sorted().toList();
        final Set<String> authorities = normalizeAuthorities(user.getAuthorities());

        final Map<String, Map<String, Boolean>> matrix = new LinkedHashMap<>();
        authorities.forEach(authority -> {
            final String[] segments = authority.split(":", 2);
            if (segments.length != 2) {
                return;
            }
            matrix.computeIfAbsent(segments[0], ignored -> new LinkedHashMap<>()).put(segments[1], true);
        });

        final Map<String, Object> body = new LinkedHashMap<>();
        body.put("username", user.getUsername());
        body.put("name", user.getName());
        body.put("roles", roleNames);
        body.put("authorities", new ArrayList<>(authorities));
        body.put("matrix", matrix);
        body.put("mfa", Map.of(
                "totpEnabled", Boolean.TRUE.equals(user.getTwoFactorEnabled()),
                "smsEnabled", Boolean.TRUE.equals(user.getSmsMfaEnabled()),
                "emailEnabled", Boolean.TRUE.equals(user.getEmailMfaEnabled()),
                "phoneVerified", Boolean.TRUE.equals(user.getPhoneVerified()),
                "emailVerified", Boolean.TRUE.equals(user.getEmailVerified()),
                "preferredFactor", user.getPreferredMfaFactor() == null ? "" : user.getPreferredMfaFactor()
        ));
        return ResponseEntity.ok(body);
    }

    private Set<String> normalizeAuthorities(final Collection<? extends GrantedAuthority> grantedAuthorities) {
        final Set<String> authorities = new LinkedHashSet<>();
        grantedAuthorities.forEach(grantedAuthority -> {
            final String value = grantedAuthority.getAuthority();
            if (value != null && !value.startsWith("ROLE_")) {
                authorities.add(value);
            }
        });
        return authorities;
    }
}

