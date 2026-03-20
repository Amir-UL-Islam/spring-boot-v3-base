package com.security.base.core.privilege;

import com.security.base.core.privilege.model.entity.Privilege;
import com.security.base.core.privilege.repository.PrivilegeRepository;
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
        if (privilegeRepository.count() != 0) {
            return;
        }
        log.info("initializing privileges");
        createIfMissing("ADMIN");
        createIfMissing("USER");
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

