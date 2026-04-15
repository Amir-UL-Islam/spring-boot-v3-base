package com.security.base.core.privilege;

import com.security.base.core.privilege.model.entity.Privilege;
import com.security.base.core.privilege.repository.PrivilegeRepository;
import com.security.base.core.security.oauth.PermissionCodes;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


@Component
@Order(1)
@Slf4j
public class PrivilegeLoader implements ApplicationRunner {

    private final PrivilegeRepository privilegeRepository;

    public PrivilegeLoader(final PrivilegeRepository privilegeRepository) {
        this.privilegeRepository = privilegeRepository;
    }

    @Override
    @Transactional
    public void run(final ApplicationArguments args) {
        log.info("initializing privileges");
        createIfMissing(PermissionCodes.ADMIN);
        createIfMissing(PermissionCodes.USER);

        createIfMissing(PermissionCodes.USER_READ);
        createIfMissing(PermissionCodes.USER_CREATE);
        createIfMissing(PermissionCodes.USER_UPDATE);
        createIfMissing(PermissionCodes.USER_DELETE);

        createIfMissing(PermissionCodes.ROLE_READ);
        createIfMissing(PermissionCodes.ROLE_CREATE);
        createIfMissing(PermissionCodes.ROLE_UPDATE);
        createIfMissing(PermissionCodes.ROLE_DELETE);

        createIfMissing(PermissionCodes.PRIVILEGE_READ);
        createIfMissing(PermissionCodes.PRIVILEGE_CREATE);
        createIfMissing(PermissionCodes.PRIVILEGE_UPDATE);
        createIfMissing(PermissionCodes.PRIVILEGE_DELETE);
        createIfMissing(PermissionCodes.PRIVILEGE_ASSIGN);

        createIfMissing(PermissionCodes.URL_READ);
        createIfMissing(PermissionCodes.URL_CREATE);
        createIfMissing(PermissionCodes.URL_UPDATE);
        createIfMissing(PermissionCodes.URL_DELETE);

        createIfMissing(PermissionCodes.MODULE_READ);
        createIfMissing(PermissionCodes.MODULE_MANAGE);

        createIfMissing(PermissionCodes.MATRIX_READ);
        createIfMissing(PermissionCodes.MATRIX_MANAGE);

        createIfMissing(PermissionCodes.POLICY_READ);
        createIfMissing(PermissionCodes.POLICY_MANAGE);

        createIfMissing(PermissionCodes.MFA_MANAGE);
        createIfMissing(PermissionCodes.MFA_CHALLENGE);
        createIfMissing(PermissionCodes.MFA_VERIFY);

        createIfMissing(PermissionCodes.AMBULANCE_READ);
        createIfMissing(PermissionCodes.AMBULANCE_CREATE);
        createIfMissing(PermissionCodes.AMBULANCE_UPDATE);
        createIfMissing(PermissionCodes.AMBULANCE_DELETE);
    }

    private void createIfMissing(final String name) {
        if (privilegeRepository.existsByNameIgnoreCase(name)) {
            return;
        }
        final Privilege privilege = new Privilege();
        privilege.setName(name);
        privilegeRepository.saveAndFlush(privilege);
    }
}

