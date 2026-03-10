package com.security.base.users;

import com.security.base.role.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(value = "/api/user", produces = MediaType.APPLICATION_JSON_VALUE)
//@PreAuthorize("hasAnyAuthority('" + UserRoles.ADMIN + "', '" + UserRoles.USER + "')")
//@SecurityRequirement(name = "bearer-jwt")
public class UsersController {

    private final UsersService usersService;
    private final RoleService roleService;

    public UsersController(final UsersService usersService, final RoleService roleService) {
        this.usersService = usersService;
        this.roleService = roleService;
    }

    @Operation(
            parameters = {
                    @Parameter(
                            name = "page",
                            in = ParameterIn.QUERY,
                            schema = @Schema(implementation = Integer.class)
                    ),
                    @Parameter(
                            name = "size",
                            in = ParameterIn.QUERY,
                            schema = @Schema(implementation = Integer.class)
                    ),
                    @Parameter(
                            name = "sort",
                            in = ParameterIn.QUERY,
                            schema = @Schema(implementation = String.class)
                    )
            }
    )
    @GetMapping
    public ResponseEntity<Page<UsersDTO>> getAllUsers(
            @RequestParam(name = "filter", required = false) final String filter,
            @Parameter(hidden = true) @SortDefault(sort = "id") @PageableDefault(size = 20) final Pageable pageable) {
        return ResponseEntity.ok(usersService.findAll(filter, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsersDTO> getUsers(@PathVariable(name = "id") final Long id) {
        return ResponseEntity.ok(usersService.get(id));
    }

    @PostMapping
    @ApiResponse(responseCode = "201")
    public ResponseEntity<Long> createUsers(@RequestBody @Valid final UsersDTO usersDTO) {
        final Long createdId = usersService.create(usersDTO);
        return new ResponseEntity<>(createdId, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Long> updateUsers(@PathVariable(name = "id") final Long id,
            @RequestBody @Valid final UsersDTO usersDTO) {
        usersService.update(id, usersDTO);
        return ResponseEntity.ok(id);
    }

    @DeleteMapping("/{id}")
    @ApiResponse(responseCode = "204")
    public ResponseEntity<Void> deleteUsers(@PathVariable(name = "id") final Long id) {
        usersService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/roleValues")
    public ResponseEntity<Map<Long, String>> getRoleValues() {
        return ResponseEntity.ok(roleService.getRoleValues());
    }


    @PostMapping("/logout-all-devices")
    public ResponseEntity<Void> logout(
    ) {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return ResponseEntity.ok().build();
        }
        usersService.logoutAllDevices(authentication.getName());
        return ResponseEntity.ok().build();
    }
}
