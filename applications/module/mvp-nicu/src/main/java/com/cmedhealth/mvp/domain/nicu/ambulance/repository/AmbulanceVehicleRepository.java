package com.cmedhealth.mvp.domain.nicu.ambulance.repository;

import com.cmedhealth.mvp.domain.nicu.ambulance.model.entity.AmbulanceVehicleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AmbulanceVehicleRepository extends JpaRepository<AmbulanceVehicleEntity, Long> {
}
