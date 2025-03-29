package com.hmtmcse.security.services;

import com.hmtmcse.security.exceptions.NotFoundException;
import com.hmtmcse.security.model.entites.Role;
import com.hmtmcse.security.model.entites.Users;
import com.hmtmcse.security.repositories.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;
    private final PrivilegeService privilegeService;
    private final UserServices userServices;

    public Role findByName(String name) {
        return roleRepository.findByName(name);
    }

    public void save(Role entity) {
        roleRepository.save(entity);
    }

    public Role getBasicRole() {
        Optional<Role> role = roleRepository.findBasicRole();
        if (role.isPresent()) {
            return role.get();
        }

        Role basicRole = new Role();
        basicRole.setCreated(new Date());
        basicRole.setName("Basic");
        basicRole.setDescription("Basic User Role");
        basicRole.setPrivileges(privilegeService.getBasicPrivileges());
        return roleRepository.save(basicRole);
    }

    public void assignRoleToUser(Long userId, Long roleId) throws NotFoundException {
        Users user = userServices.findById(userId);
        Role role = roleRepository.findById(roleId).orElseThrow(() -> new NotFoundException("Role not found"));
        user.getRoles().add(role);
        userServices.save(user);
    }
}
