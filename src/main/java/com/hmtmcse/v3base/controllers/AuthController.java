package com.hmtmcse.v3base.controllers;

import com.hmtmcse.v3base.model.dtos.request.Authenticate;
import com.hmtmcse.v3base.model.dtos.request.Registration;
import com.hmtmcse.v3base.model.dtos.response.SuccessfulAuthentication;
import com.hmtmcse.v3base.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService service;
    @PostMapping("/register")
    public SuccessfulAuthentication register(@RequestBody Registration dto) {
        return service.register(dto);
    }

    @PostMapping("/login")
    public SuccessfulAuthentication login(@RequestBody Authenticate dto) {
        return service.login(dto);
    }
}
