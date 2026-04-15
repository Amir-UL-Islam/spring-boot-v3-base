package com.security.base.core.security.policy;

import com.security.base.core.security.oauth.PermissionCodes;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/policy/drift")
@RequiredArgsConstructor
@Tag(name = "Policy Drift", description = "Policy consistency diagnostics for matrix authorization")
public class PolicyDriftController {

    private final PolicyDriftService policyDriftService;

    @GetMapping
    @PreAuthorize("hasAuthority('" + PermissionCodes.MATRIX_MANAGE + "')")
    public ResponseEntity<PolicyDriftReport> getDriftReport() {
        return ResponseEntity.ok(policyDriftService.analyzeDrift());
    }
}

