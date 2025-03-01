package com.hmtmcse.v3base.model.mappers;

import com.hmtmcse.v3base.exceptions.NotFoundException;
import com.hmtmcse.v3base.model.dtos.RoleDTO;
import com.hmtmcse.v3base.model.entites.Role;
import com.hmtmcse.v3base.services.PrivilegeService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.stream.Collectors;

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
