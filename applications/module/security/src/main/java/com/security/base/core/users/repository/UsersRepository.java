package com.security.base.core.users.repository;

import java.util.List;

import com.security.base.core.users.model.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UsersRepository extends JpaRepository<Users, Long> {

    @EntityGraph(attributePaths = "role")
    Users findByUsernameIgnoreCase(String username);

    @EntityGraph(attributePaths = "role")
    Users findByEmail(String email);

    Page<Users> findAllById(Long id, Pageable pageable);

    boolean existsByUsernameIgnoreCase(String username);

    List<Users> findAllByRoleId(Long id);

}
