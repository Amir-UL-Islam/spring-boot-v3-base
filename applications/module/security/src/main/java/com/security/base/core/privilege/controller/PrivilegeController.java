package com.security.base.core.privilege.controller;

import com.security.base.core.privilege.model.dto.PrivilegeDTO;
import com.security.base.core.privilege.service.implmentation.PrivilegeService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(value = "/api/privileges", produces = MediaType.APPLICATION_JSON_VALUE)
//@PreAuthorize("hasAuthority('" + UserRoles.ADMIN + "')")
//@SecurityRequirement(name = "oauth2-password")
@Tag(name = "Privilege", description = "APIs for managing user privileges and their associated URLs")
public class PrivilegeController {

    private final PrivilegeService privilegeService;

    public PrivilegeController(final PrivilegeService privilegeService) {
        this.privilegeService = privilegeService;
    }

    @GetMapping
    public ResponseEntity<List<PrivilegeDTO>> getAllPrivileges() {
        return ResponseEntity.ok(privilegeService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PrivilegeDTO> getPrivilege(@PathVariable(name = "id") final Long id) {
        return ResponseEntity.ok(privilegeService.get(id));
    }

    @PostMapping
    @ApiResponse(responseCode = "201")
    public ResponseEntity<Long> createPrivilege(
            @RequestBody @Valid final PrivilegeDTO privilegeDTO) {
        final Long createdId = privilegeService.create(privilegeDTO);
        return new ResponseEntity<>(createdId, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Long> updatePrivilege(@PathVariable(name = "id") final Long id,
                                                @RequestBody @Valid final PrivilegeDTO privilegeDTO) {
        privilegeService.update(id, privilegeDTO);
        return ResponseEntity.ok(id);
    }

    @PatchMapping("/assign/url")
    @ApiResponse(responseCode = "201")
    public ResponseEntity<Long> assignUrl(@RequestParam(name = "privilegeId") final Long privilegeId,
                                          @RequestParam(name = "urlId") final Long urlId) {
        privilegeService.assignUrl(privilegeId, urlId);
        return ResponseEntity.ok(privilegeId);
    }

    @PatchMapping("/remove/assess/url")
    @ApiResponse(responseCode = "201")
    public ResponseEntity<Long> removeUrl(@RequestParam(name = "privilegeId") final Long privilegeId,
                                          @RequestParam(name = "urlId") final Long urlId) {
        privilegeService.removeAssignUrl(privilegeId, urlId);
        return ResponseEntity.ok(privilegeId);
    }

    @DeleteMapping("/{id}")
    @ApiResponse(responseCode = "204")
    public ResponseEntity<Void> deletePrivilege(@PathVariable(name = "id") final Long id) {
        privilegeService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
