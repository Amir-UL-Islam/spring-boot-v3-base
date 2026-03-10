package com.security.base.role;

import com.security.base.privilege.Privilege;
import com.security.base.privilege.PrivilegeRepository;
import jakarta.transaction.Transactional;
import java.util.Optional;
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
        if (roleRepository.count() != 0) {
            return;
        }
        log.info("initializing roles");

        final Optional<Privilege> adminPrivilege = privilegeRepository.findByNameIgnoreCase("ADMIN");
        final Optional<Privilege> userPrivilege = privilegeRepository.findByNameIgnoreCase("USER");
        if (adminPrivilege.isEmpty() || userPrivilege.isEmpty()) {
            log.warn("privileges missing, skipping role seeding");
            return;
        }

        final Role adminRole = new Role();
        adminRole.setName("ADMIN");
        adminRole.setDescription("Admin Description");
        adminRole.setPrivilege(Set.of(adminPrivilege.get()));
        roleRepository.save(adminRole);

        final Role userRole = new Role();
        userRole.setName("USER");
        userRole.setDescription("Demo Description");
        userRole.setPrivilege(Set.of(userPrivilege.get()));
        roleRepository.save(userRole);
    }

}
