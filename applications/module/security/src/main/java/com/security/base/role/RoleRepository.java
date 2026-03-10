package com.security.base.role;

import java.util.List;
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
