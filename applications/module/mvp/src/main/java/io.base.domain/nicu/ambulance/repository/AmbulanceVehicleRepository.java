package io.base.domain.nicu.ambulance.repository;

import io.base.domain.nicu.ambulance.model.entity.AmbulanceVehicleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AmbulanceVehicleRepository extends JpaRepository<AmbulanceVehicleEntity, Long> {
}
