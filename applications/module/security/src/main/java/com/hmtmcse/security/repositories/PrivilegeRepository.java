package com.hmtmcse.security.repositories;

import com.hmtmcse.security.model.entites.Privilege;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PrivilegeRepository extends JpaRepository<Privilege, Long> {
    @Query("SELECT p FROM Privilege p WHERE p.urlPattern = ?1 AND p.httpMethod = ?2")
    List<Privilege> findByUrlPatternAndHttpMethod(String urlPattern, String httpMethod);

    @Query("SELECT p FROM Privilege p WHERE  p.isBasic=TRUE ")
    List<Privilege> getBasicPrivileges();
}
