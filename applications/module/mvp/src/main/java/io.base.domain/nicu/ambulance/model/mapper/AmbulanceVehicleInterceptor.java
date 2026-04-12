package io.base.domain.nicu.ambulance.model.mapper;

import com.problemfighter.pfspring.restapi.inter.CopyInterceptor;
import io.base.domain.nicu.ambulance.model.dto.AmbulanceVehicleDTO;
import io.base.domain.nicu.ambulance.model.entity.AmbulanceVehicleEntity;
import io.base.domain.nicu.ambulance.service.AmbulanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AmbulanceVehicleInterceptor implements CopyInterceptor<AmbulanceVehicleEntity, AmbulanceVehicleDTO, AmbulanceVehicleDTO> {
    private final AmbulanceService ambulanceService;

    @Override
    public void meAsSrc(AmbulanceVehicleDTO source, AmbulanceVehicleEntity destination) {
        destination.setAmbulance(ambulanceService.findById(source.getAmbulanceId()).orElse(null));

    }

    @Override
    public void meAsDst(AmbulanceVehicleEntity source, AmbulanceVehicleDTO destination) {
        if (source.getAmbulance() != null) {
            destination.setAmbulanceId(source.getAmbulance().getId());
        }
    }
}
