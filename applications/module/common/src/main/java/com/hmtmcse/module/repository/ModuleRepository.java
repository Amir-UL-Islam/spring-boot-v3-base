package com.hmtmcse.module.repository;

import com.hmtmcse.module.entity.ModuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing module persistence
 */
@Repository
public interface ModuleRepository extends JpaRepository<ModuleEntity, Long> {

    Optional<ModuleEntity> findByModuleId(String moduleId);

    List<ModuleEntity> findByEnabled(boolean enabled);

    @Query("SELECT m FROM ModuleEntity m WHERE m.state = 'STARTED' AND m.enabled = true")
    List<ModuleEntity> findActiveModules();

    @Query("SELECT m FROM ModuleEntity m WHERE m.required = true")
    List<ModuleEntity> findRequiredModules();

    boolean existsByModuleId(String moduleId);
}

