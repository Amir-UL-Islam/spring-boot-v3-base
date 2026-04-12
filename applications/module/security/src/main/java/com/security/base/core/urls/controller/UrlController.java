package com.security.base.core.urls.controller;

import com.security.base.core.privilege.service.implmentation.PrivilegeService;
import com.security.base.core.urls.model.dto.UrlDTO;
import com.security.base.core.urls.service.UrlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
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
@RequestMapping(value = "/api/url", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "URLs", description = "APIs for managing URLs and their associated privileges")
//@PreAuthorize("hasAuthority('" + UserRoles.ADMIN + "')")
//@SecurityRequirement(name = "bearer-jwt")
public class UrlController {

    private final UrlService urlsService;
    private final PrivilegeService privilegeService;

    public UrlController(final UrlService urlsService, final PrivilegeService privilegeService) {
        this.urlsService = urlsService;
        this.privilegeService = privilegeService;
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
    @Transactional(readOnly = true)
    public ResponseEntity<Page<UrlDTO>> getAllUrls(
            @RequestParam(name = "filter", required = false) final String filter,
            @Parameter(hidden = true) @SortDefault(sort = "id") @PageableDefault(size = 20) final Pageable pageable) {
        return ResponseEntity.ok(urlsService.findAll(filter, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UrlDTO> getUrls(@PathVariable final Long id) {
        return ResponseEntity.ok(urlsService.get(id));
    }

    @GetMapping("/privilege/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<Set<UrlDTO>> getByPrivilegeId(@PathVariable final Long id) {
        return ResponseEntity.ok(urlsService.getByPrivilege(id));
    }

    @PostMapping
    @ApiResponse(responseCode = "201")
    public ResponseEntity<Long> createUrls(@RequestBody @Valid final UrlDTO urlsDTO) {
        final Long createdId = urlsService.create(urlsDTO);
        return new ResponseEntity<>(createdId, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Long> updateUrls(@PathVariable final Long id,
                                           @RequestBody @Valid final UrlDTO urlsDTO) {
        urlsService.update(id, urlsDTO);
        return ResponseEntity.ok(id);
    }

    @DeleteMapping("/{id}")
    @ApiResponse(responseCode = "204")
    @Transactional
    public ResponseEntity<Void> deleteUrls(@PathVariable final Long id) {
        urlsService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/privilegeValues")
    public ResponseEntity<Map<Long, String>> getPrivilegeValues() {
        return ResponseEntity.ok(privilegeService.getPrivilegeValues());
    }

}
