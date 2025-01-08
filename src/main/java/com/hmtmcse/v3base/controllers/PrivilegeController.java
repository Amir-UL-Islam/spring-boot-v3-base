package com.hmtmcse.v3base.controllers;

import com.hmtmcse.v3base.model.dtos.PrivilegeDTO;
import com.hmtmcse.v3base.model.entites.Privilege;
import com.hmtmcse.v3base.model.mappers.PrivilegeMapper;
import com.hmtmcse.v3base.services.PrivilegeService;
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
