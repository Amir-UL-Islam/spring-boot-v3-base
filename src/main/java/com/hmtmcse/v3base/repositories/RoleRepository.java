package com.hmtmcse.v3base.repositories;

import com.hmtmcse.v3base.model.entites.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    @Query("SELECT r FROM Role r WHERE r.name = ?1")
    Role findByName(String name);

    @Query("SELECT r FROM Role r WHERE r.name = 'Basic'")
    Optional<Role> findBasicRole();
}
