package com.cmedhealth.mvp.domain.nicu.ambulance.model.mapper;

import com.problemfighter.pfspring.restapi.inter.CopyInterceptor;
import com.problemfighter.pfspring.restapi.rr.RequestResponse;
import com.security.base.core.users.service.UsersService;
import com.cmedhealth.mvp.domain.nicu.ambulance.model.dto.AmbulanceDTO;
import com.cmedhealth.mvp.domain.nicu.ambulance.model.entity.AmbulanceEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;



@Component
@RequiredArgsConstructor
public class AmbulanceInterceptor implements CopyInterceptor<AmbulanceEntity, AmbulanceDTO, AmbulanceDTO>, RequestResponse {

    private final UsersService usersService;

    @Override
    public void meAsSrc(AmbulanceDTO source, AmbulanceEntity destination) {
        destination.setOwner(usersService.findById(source.getOwnerId()).orElse(null));
    }

    @Override
    public void meAsDst(AmbulanceEntity source, AmbulanceDTO destination) {
        if (source.getOwner() != null) {
            destination.setOwnerId(source.getOwner().getId());
        }
    }
}
