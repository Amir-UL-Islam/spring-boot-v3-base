package com.hmtmcse.security.controllers;

import com.hmtmcse.security.exceptions.NotFoundException;
import com.hmtmcse.security.model.dtos.RoleDTO;
import com.hmtmcse.security.model.entites.Role;
import com.hmtmcse.security.model.mappers.RoleMapper;
import com.hmtmcse.security.services.RoleService;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RoleController {
    private final RoleService roleService;
    private final RoleMapper roleMapper;
    private static final Logger logger = LoggerFactory.getLogger(RoleController.class);


    @PostMapping("/api/v1/roles")
    @Transactional(rollbackFor = {NotFoundException.class})
    public void save(@RequestBody RoleDTO dto) throws NotFoundException {
        Role role = roleMapper.map(dto);
        roleService.save(role);
    }

    @PostMapping("/api/v1/roles/assign-role-to-user")
    @Transactional
    public void assignRoleToUser(Long userId, Long roleId) throws NotFoundException {
        roleService.assignRoleToUser(userId, roleId);
    }
}
