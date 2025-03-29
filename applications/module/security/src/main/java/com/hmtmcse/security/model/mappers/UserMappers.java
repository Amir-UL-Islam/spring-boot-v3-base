package com.hmtmcse.security.model.mappers;

import com.hmtmcse.security.model.dtos.request.Registration;
import com.hmtmcse.security.model.entites.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RequiredArgsConstructor
public class UserMappers {
    private final PasswordEncoder passwordEncoder;

    public Users map(Registration dto) {
        Users entity = new Users();
        entity.setCreated(new Date(dto.getCreated() == null || dto.getCreated() == 0 ? System.currentTimeMillis() : dto.getCreated()));
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
