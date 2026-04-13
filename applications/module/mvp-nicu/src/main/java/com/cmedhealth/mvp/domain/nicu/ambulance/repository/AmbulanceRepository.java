package com.cmedhealth.mvp.domain.nicu.ambulance.repository;

import com.cmedhealth.mvp.domain.nicu.ambulance.model.entity.AmbulanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AmbulanceRepository extends JpaRepository<AmbulanceEntity, Long> {
}
