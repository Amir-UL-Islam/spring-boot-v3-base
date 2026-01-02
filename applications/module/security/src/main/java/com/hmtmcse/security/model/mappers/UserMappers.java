package com.hmtmcse.security.model.mappers;

import com.hmtmcse.security.model.dtos.request.Registration;
import com.hmtmcse.security.model.entites.RegisteredUsers;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMappers {
    private final PasswordEncoder passwordEncoder;

    public RegisteredUsers map(Registration dto) {
        RegisteredUsers entity = new RegisteredUsers();
        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());
        entity.setUsername(dto.getUsername());
        entity.setEmail(dto.getEmail());
        entity.setPhone(dto.getPhone());
        entity.setGender(dto.getGender());
        entity.setPassword(passwordEncoder.encode(dto.getPassword()));
        return entity;
    }
}
