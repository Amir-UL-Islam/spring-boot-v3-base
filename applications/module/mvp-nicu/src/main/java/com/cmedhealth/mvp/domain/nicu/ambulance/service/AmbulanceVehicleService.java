package com.cmedhealth.mvp.domain.nicu.ambulance.service;

import com.problemfighter.pfspring.restapi.rr.RequestResponse;
import com.cmedhealth.mvp.domain.nicu.ambulance.model.entity.AmbulanceVehicleEntity;
import com.cmedhealth.mvp.domain.nicu.ambulance.repository.AmbulanceVehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AmbulanceVehicleService implements RequestResponse {
    private final AmbulanceVehicleRepository ambulanceVehicleRepository;

    public Optional<AmbulanceVehicleEntity> findById(Long id){
        return ambulanceVehicleRepository.findById(id);
    }
}
