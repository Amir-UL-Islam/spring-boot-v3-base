package com.hmtmcse.security.controllers;

import com.hmtmcse.security.model.dtos.request.Authenticate;
import com.hmtmcse.security.model.dtos.request.Registration;
import com.hmtmcse.security.model.dtos.response.SuccessfulAuthentication;
import com.hmtmcse.security.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
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
    @Transactional
//    from users
//    from user_role
//    from role_privilege
//    from users
//    from user_role
//    from role_privilege
    public SuccessfulAuthentication login(@RequestBody Authenticate dto) {
        return service.login(dto);
    }
}
