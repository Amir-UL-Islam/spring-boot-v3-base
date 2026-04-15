package com.security.base.core.users;

import com.security.base.core.role.model.entity.Role;
import com.security.base.core.role.repository.RoleRepository;
import com.security.base.core.security.oauth.UserRoles;
import com.security.base.core.users.model.entity.Users;
import com.security.base.core.users.repository.UsersRepository;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Component
@Order(5)
@Slf4j
public class UserLoader implements ApplicationRunner {

    private final UsersRepository usersRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserLoader(final UsersRepository usersRepository,
                      final RoleRepository roleRepository,
                      final PasswordEncoder passwordEncoder) {
        this.usersRepository = usersRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(final ApplicationArguments args) {
        log.info("initializing default users");
        upsertUser(
                "superadmin",
                "superadmin@local.dev",
                "Super Admin",
                "superadmin123!",
                Set.of(UserRoles.SUPER_ADMIN)
        );
        upsertUser(
                "admin",
                "admin@local.dev",
                "Administrator",
                "admin123!",
                Set.of(UserRoles.ADMIN)
        );
        upsertUser(
                "user",
                "user@local.dev",
                "Standard User",
                "user123!",
                Set.of(UserRoles.USER)
        );
    }

    private void upsertUser(final String username,
                            final String email,
                            final String name,
                            final String rawPassword,
                            final Set<String> roleNames) {
        Users user = usersRepository.findByUsernameIgnoreCase(username);
        if (user == null) {
            user = new Users();
            user.setUsername(username);
        }

        user.setEmail(email);
        user.setName(name);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setTwoFactorEnabled(false);

        final Set<Role> roles = new LinkedHashSet<>();
        roleNames.forEach(roleName -> {
            final Role role = roleRepository.findByName(roleName);
            if (role != null) {
                roles.add(role);
            }
        });
        user.setRole(roles);

        if (user.getTokenVersion() == 0) {
            user.setTokenVersion(1);
        }

        usersRepository.save(user);
    }
}

