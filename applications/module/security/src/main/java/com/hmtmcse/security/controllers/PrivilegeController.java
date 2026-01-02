package com.hmtmcse.security.controllers;

import com.hmtmcse.security.model.dtos.PrivilegeDTO;
import com.hmtmcse.security.model.entites.Privilege;
import com.hmtmcse.security.model.mappers.PrivilegeMapper;
import com.hmtmcse.security.services.PrivilegeService;
import com.problemfighter.pfspring.restapi.rr.response.DetailsResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/api/v1/privilege/{id}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Transactional
//    from users
//    from user_role
//    from role_privilege
//    from privilege
//    from privilege
    public DetailsResponse<PrivilegeDTO> getPrivilegeById(
            @PathVariable Long id
    ) {
        return privilegeService.getPrivilege(id);
    }

}
