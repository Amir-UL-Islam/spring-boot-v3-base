package com.hmtmcse.security.repositories;

import com.hmtmcse.security.model.entites.RegisteredUsers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<RegisteredUsers, Long> {
    Optional<RegisteredUsers> findByUsername(String username);
}
