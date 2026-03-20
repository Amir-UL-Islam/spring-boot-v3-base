package com.security.base.core.role.repository;

import java.util.List;

import com.security.base.core.role.model.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface RoleRepository extends JpaRepository<Role, Long> {

    Role findByName(String name);

    Page<Role> findAllById(Long id, Pageable pageable);

    List<Role> findAllByPrivilegeId(Long id);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByDescriptionIgnoreCase(String description);

}
