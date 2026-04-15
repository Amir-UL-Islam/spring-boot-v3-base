package com.security.base.core.urls.repository;

import com.security.base.core.urls.model.entity.Url;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.Set;


public interface UrlsRepository extends JpaRepository<Url, Long> {

    Page<Url> findAllById(Long id, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"privileges"})
    Optional<Url> findById(Long aLong);

    @Query("SELECT u FROM Url u JOIN u.privileges p WHERE p.id = :id")
    Set<Url> findAllByPrivilegeId(Long id);

    @EntityGraph(attributePaths = {"privileges"})
    Optional<Url> findByEndpointIgnoreCaseAndMethodIgnoreCase(String endpoint, String method);

    boolean existsByEndpointIgnoreCase(String endpoint);

    boolean existsByEndpointIgnoreCaseAndMethodIgnoreCase(String endpoint, String method);

}
