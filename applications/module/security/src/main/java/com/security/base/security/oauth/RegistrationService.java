package com.security.base.security.oauth;

import com.security.base.role.RoleRepository;
import com.security.base.users.Users;
import com.security.base.users.UsersRepository;

import java.util.Random;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@Slf4j
@RequiredArgsConstructor
public class RegistrationService {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    public void register(final RegistrationRequest registrationRequest) {
        log.info("registering new user: {}", registrationRequest.getUsername());

        final Users users = new Users();
        users.setName(registrationRequest.getName());
        users.setUsername(registrationRequest.getUsername());
        users.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
        // assign default role
        users.setRole(Set.of(roleRepository.findByName(UserRoles.ADMIN)));
        users.setTokenVersion(new Random().nextInt());
        usersRepository.save(users);
    }

}
