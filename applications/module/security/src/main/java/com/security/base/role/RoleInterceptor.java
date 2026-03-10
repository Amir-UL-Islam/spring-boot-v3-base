package com.security.base.role;

import com.security.base.privilege.Privilege;
import com.security.base.privilege.PrivilegeRepository;
import com.security.base.util.NotFoundException;
import com.problemfighter.pfspring.restapi.inter.CopyInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RoleInterceptor implements CopyInterceptor<Role, RoleDTO, RoleDTO> {
    private final PrivilegeRepository privilegeRepository;

    @Override
    public void meAsSrc(RoleDTO source, Role destination) {
        // Only handle privilege relationship - name and description are auto-mapped by library
        final List<Privilege> privileges = privilegeRepository.findAllById(
                source.getPrivilege() == null ? List.of() : source.getPrivilege());

        if (privileges.size() != (source.getPrivilege() == null ? 0 : source.getPrivilege().size())) {
            throw new NotFoundException("one of privilege not found");
        }

        destination.setPrivilege(new HashSet<>(privileges));
    }

    @Override
    public void meAsDst(Role source, RoleDTO destination) {
        // Only handle privilege relationship - id, name, and description are auto-mapped by library
        destination.setPrivilege(source.getPrivilege().stream()
                .map(privilege -> privilege.getId())
                .toList());
    }
}
