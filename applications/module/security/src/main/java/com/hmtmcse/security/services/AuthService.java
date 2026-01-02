package com.hmtmcse.security.services;

import com.hmtmcse.security.model.dtos.request.Authenticate;
import com.hmtmcse.security.model.dtos.request.Registration;
import com.hmtmcse.security.model.dtos.response.SuccessfulAuthentication;
import com.hmtmcse.security.model.entites.RegisteredUsers;
import com.hmtmcse.security.model.mappers.UserMappers;
import com.hmtmcse.security.repositories.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;


@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final UsersRepository repository;
    private final JwtTokenService jwtTokenService;
    private final UserMappers userMapper;
    private final RoleService roleService;

    @Transactional
    public SuccessfulAuthentication register(Registration dto) {
        RegisteredUsers newUser = userMapper.map(dto);
        newUser.getRoles().add(roleService.getBasicRole());
        newUser = repository.save(newUser);

        String token = jwtTokenService.createToken(newUser);
        return SuccessfulAuthentication.builder()
                .username(newUser.getUsername())
                .token(token)
                .build();
    }

    public SuccessfulAuthentication login(Authenticate dto) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            dto.getUsername(),
                            dto.getPassword()
                    )
            );
        } catch (LockedException e) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "User account is locked", e);
        }
        RegisteredUsers user = repository.findByUsername(dto.getUsername()).orElseThrow(
                () -> new UsernameNotFoundException("User not found")
        );
        String token = jwtTokenService.createToken(user);
        return SuccessfulAuthentication.builder()
                .username(user.getUsername())
                .token(token)
                .build();
    }
}
