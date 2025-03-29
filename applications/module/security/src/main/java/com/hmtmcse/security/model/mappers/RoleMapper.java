package com.hmtmcse.security.model.mappers;

import com.hmtmcse.security.exceptions.NotFoundException;
import com.hmtmcse.security.model.dtos.RoleDTO;
import com.hmtmcse.security.model.entites.Role;
import com.hmtmcse.security.services.PrivilegeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RequiredArgsConstructor
public class RoleMapper {
    private final PrivilegeService privilegeService;

    public Role map(RoleDTO dto) throws NotFoundException {
        Role entity = new Role();
        entity.setCreated(new Date(dto.getCreated() == null || dto.getCreated() == 0 ? System.currentTimeMillis() : dto.getCreated()));
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        for (Long id : dto.getPrivilegeIds()) {
            entity.getPrivileges().add(privilegeService.findById(id));
        }
        return entity;
    }
}
