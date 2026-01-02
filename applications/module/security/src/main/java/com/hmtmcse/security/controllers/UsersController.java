package com.hmtmcse.security.controllers;

import com.hmtmcse.security.model.entites.RegisteredUsers;
import com.hmtmcse.security.services.UserServices;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UsersController {
    private final UserServices service;


    @Operation(summary = "Welcome", description = "Get welcome message")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/welcome")
    public String root() {
        return "Welcome to the Central Security System!";
    }

    @Operation(summary = "Get all user", description = "Get all user")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/get-all")
    public List<RegisteredUsers> getUsers() {
        return service.getUsers();
    }

    @Operation(summary = "Test Admin Role", description = "Test Admin Role")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/test-admin-role")
    public String getTestAdminRole() {
        return "You have the admin role!";
    }

    @Operation(summary = "Test User Role", description = "Test User Role")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/test-user-role")
    public String getTestUserRole() {
        return "You have the user role!";
    }

    @Operation(summary = "Get user by id", description = "Get user by id")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/get-user-by-id")
    public RegisteredUsers getUserById(Long id) {
        return service.findById(id);
    }
}
