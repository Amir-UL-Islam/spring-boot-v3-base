package com.security.base.core.role;

import com.security.base.core.privilege.model.entity.Privilege;
import com.security.base.core.privilege.repository.PrivilegeRepository;
import com.security.base.core.security.oauth.PermissionCodes;
import com.security.base.core.security.oauth.UserRoles;
import com.security.base.core.role.model.entity.Role;
import com.security.base.core.role.repository.RoleRepository;
import jakarta.transaction.Transactional;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


@Component
@Order(3)
@Slf4j
public class RoleLoader implements ApplicationRunner {

    private final RoleRepository roleRepository;
    private final PrivilegeRepository privilegeRepository;

    public RoleLoader(final RoleRepository roleRepository,
                      final PrivilegeRepository privilegeRepository) {
        this.roleRepository = roleRepository;
        this.privilegeRepository = privilegeRepository;
    }

    @Override
    @Transactional
    public void run(final ApplicationArguments args) {
        log.info("initializing roles");

        upsertRole(UserRoles.SUPER_ADMIN, "Super Administrator",
                resolveAllPrivileges());

        upsertRole(UserRoles.ADMIN, "Administrator",
                resolvePrivileges(
                        PermissionCodes.ADMIN,
                        PermissionCodes.USER,
                        PermissionCodes.USER_READ,
                        PermissionCodes.USER_CREATE,
                        PermissionCodes.USER_UPDATE,
                        PermissionCodes.USER_DELETE,
                        PermissionCodes.ROLE_READ,
                        PermissionCodes.PRIVILEGE_READ,
                        PermissionCodes.URL_READ,
                        PermissionCodes.MODULE_READ,
                        PermissionCodes.MATRIX_READ,
                        PermissionCodes.AMBULANCE_READ,
                        PermissionCodes.AMBULANCE_CREATE,
                        PermissionCodes.AMBULANCE_UPDATE
                ));

        upsertRole(UserRoles.USER, "Basic User",
                resolvePrivileges(
                        PermissionCodes.USER,
                        PermissionCodes.USER_READ,
                        PermissionCodes.MATRIX_READ,
                        PermissionCodes.AMBULANCE_READ,
                        PermissionCodes.AMBULANCE_CREATE
                ));
    }

    private Set<Privilege> resolveAllPrivileges() {
        return new LinkedHashSet<>(privilegeRepository.findAll());
    }

    private Set<Privilege> resolvePrivileges(final String... privilegeNames) {
        final Set<Privilege> privileges = new LinkedHashSet<>();
        Arrays.stream(privilegeNames)
                .map(name -> privilegeRepository.findByNameIgnoreCase(name).orElse(null))
                .filter(Objects::nonNull)
                .forEach(privileges::add);
        return privileges;
    }

    private void upsertRole(final String roleName, final String description, final Set<Privilege> privileges) {
        Role role = roleRepository.findAll().stream()
                .filter(r -> roleName.equalsIgnoreCase(r.getName()))
                .findFirst()
                .orElseGet(Role::new);
        role.setName(roleName);
        role.setDescription(description);
        role.setPrivilege(privileges);
        roleRepository.save(role);
    }

}
