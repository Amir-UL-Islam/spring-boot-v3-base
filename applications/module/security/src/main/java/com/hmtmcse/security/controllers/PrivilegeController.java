package com.hmtmcse.security.controllers;

import com.hmtmcse.security.model.dtos.PrivilegeDTO;
import com.hmtmcse.security.model.entites.Privilege;
import com.hmtmcse.security.model.mappers.PrivilegeMapper;
import com.hmtmcse.security.services.PrivilegeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PrivilegeController {
    private final PrivilegeService privilegeService;
    private final PrivilegeMapper privilegeMapper;

    @PostMapping("/api/v1/privileges")
    public void savePrivilege(@RequestBody PrivilegeDTO dto) {
        Privilege privilege = privilegeMapper.map(dto);
        privilegeService.save(privilege);
    }

}
