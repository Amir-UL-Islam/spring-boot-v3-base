package io.base.domain.nicu.ambulance.repository;

import io.base.domain.nicu.ambulance.model.entity.AmbulanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AmbulanceRepository extends JpaRepository<AmbulanceEntity, Long> {
}
