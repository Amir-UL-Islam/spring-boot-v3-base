package com.security.base.core.users.model.mapper;

import com.security.base.core.role.model.entity.Role;
import com.security.base.core.role.repository.RoleRepository;
import com.security.base.core.users.model.dto.UsersDTO;
import com.security.base.core.users.model.entity.Users;
import com.security.base.util.NotFoundException;
import com.problemfighter.pfspring.restapi.inter.CopyInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UsersInterceptor implements CopyInterceptor<Users, UsersDTO, UsersDTO> {

    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void meAsSrc(UsersDTO source, Users destination) {
        // Only handle custom logic:
        // 1. Password encoding (transformation)
        // 2. Role relationship (ID -> Entity)
        // Note: name, email, username, twoFactorEnabled, totpSecret are auto-mapped by library

        destination.setPassword(passwordEncoder.encode(source.getPassword()));

        // Handle role relationships
        final List<Role> roles = roleRepository.findAllById(
                source.getRole() == null ? List.of() : source.getRole());

        if (roles.size() != (source.getRole() == null ? 0 : source.getRole().size())) {
            throw new NotFoundException("one of role not found");
        }

        destination.setRole(new HashSet<>(roles));
    }

    @Override
    public void meAsDst(Users source, UsersDTO destination) {
        // Only handle role relationship (Entity -> ID list)
        // Note: id, name, email, username, twoFactorEnabled, totpSecret are auto-mapped by library
        // Note: password is NOT mapped (security - never send password to client)

        destination.setRole(source.getRole().stream()
                .map(role -> role.getId())
                .toList());
    }
}
