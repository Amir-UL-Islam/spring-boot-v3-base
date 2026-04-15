package com.security.base.core.privilege.repository;

import com.security.base.core.privilege.model.entity.Privilege;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;


public interface PrivilegeRepository extends JpaRepository<Privilege, Long> {

    boolean existsByNameIgnoreCase(String name);

    @EntityGraph(attributePaths = {"urls"})
    Optional<Privilege> findByNameIgnoreCase(String name);

    @Query("SELECT p FROM Privilege p JOIN p.urls u " +
            "WHERE (u.endpoint = :urlPattern OR :urlPattern LIKE CONCAT(u.endpoint, '/%')) " +
            "AND LOWER(u.method) = LOWER(:httpMethod)")
    List<Privilege> findByUrlPatternAndHttpMethod(String urlPattern, String httpMethod);


}
