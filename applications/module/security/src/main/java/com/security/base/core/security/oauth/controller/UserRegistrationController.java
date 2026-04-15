package com.security.base.core.security.oauth.controller;

import com.security.base.core.security.oauth.dto.RegistrationInitRequest;
import com.security.base.core.security.oauth.dto.RegistrationInitResponse;
import com.security.base.core.security.oauth.dto.RegistrationVerifyRequest;
import com.security.base.core.security.oauth.service.RegistrationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
@Tag(name = "User Registration", description = "APIs for user registration and account management")
public class UserRegistrationController {

    private final RegistrationService registrationService;

    public UserRegistrationController(final RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/register/init")
    public ResponseEntity<RegistrationInitResponse> registerInit(
            @RequestBody @Valid final RegistrationInitRequest request) {
        return ResponseEntity.ok(registrationService.initRegistration(request));
    }

    @PostMapping("/register/verify")
    public ResponseEntity<Void> registerVerify(
            @RequestBody @Valid final RegistrationVerifyRequest request) {
        registrationService.verifyRegistration(request);
        return ResponseEntity.noContent().build();
    }
}
