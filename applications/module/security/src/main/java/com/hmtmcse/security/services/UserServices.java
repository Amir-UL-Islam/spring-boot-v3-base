package com.hmtmcse.security.services;

import com.hmtmcse.security.model.entites.UserAuth;
import com.hmtmcse.security.model.entites.Users;
import com.hmtmcse.security.repositories.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServices implements UserDetailsService {
    private final UsersRepository repository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return new UserAuth(repository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found")));

    }

    public List<Users> getUsers() {
        return repository.findAll();
    }

    public Users findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public void save(Users user) {
        repository.save(user);
    }
}
