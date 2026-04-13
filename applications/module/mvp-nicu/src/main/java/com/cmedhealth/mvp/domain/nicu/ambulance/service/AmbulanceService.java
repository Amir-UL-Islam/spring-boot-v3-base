package com.cmedhealth.mvp.domain.nicu.ambulance.service;

import com.problemfighter.pfspring.restapi.inter.RestApiAction;
import com.problemfighter.pfspring.restapi.rr.RequestResponse;
import com.problemfighter.pfspring.restapi.rr.request.RequestBulkData;
import com.problemfighter.pfspring.restapi.rr.request.RequestData;
import com.problemfighter.pfspring.restapi.rr.response.BulkErrorValidEntities;
import com.problemfighter.pfspring.restapi.rr.response.BulkResponse;
import com.problemfighter.pfspring.restapi.rr.response.DetailsResponse;
import com.problemfighter.pfspring.restapi.rr.response.MessageResponse;
import com.security.base.core.users.service.UsersService;
import com.cmedhealth.mvp.domain.nicu.ambulance.model.dto.AmbulanceDTO;
import com.cmedhealth.mvp.domain.nicu.ambulance.model.entity.AmbulanceEntity;
import com.cmedhealth.mvp.domain.nicu.ambulance.repository.AmbulanceRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AmbulanceService implements RequestResponse, RestApiAction<AmbulanceDTO, AmbulanceDTO, AmbulanceDTO> {

    private final UsersService usersService;
    private final AmbulanceRepository ambulanceRepository;

    public Optional<AmbulanceEntity> findById(Long id) {
        return ambulanceRepository.findById(id);
    }

    public DetailsResponse<AmbulanceDTO> save(@Valid RequestData<AmbulanceDTO> data) {
        requestProcessor().validateId(data.getData().getOwnerId(), "Owner ID can't be null");
        dataUtil().validateAndOptionToEntity(usersService.findById(data.getData().getOwnerId()), "Owner not found with ID: " + data.getData().getOwnerId());

        AmbulanceEntity entity = new AmbulanceEntity();
        requestProcessor().process(data.getData(), entity);
        entity = ambulanceRepository.save(entity);
        return responseProcessor().response(entity, AmbulanceDTO.class);
    }

    @Override
    public MessageResponse create(@Valid RequestData<AmbulanceDTO> data) {
        AmbulanceEntity entity = new AmbulanceEntity();
        requestProcessor().process(data.getData(), entity);
        entity = ambulanceRepository.save(entity);
        return responseProcessor().response("Ambulance registered successfully with ID: " + entity.getId());
    }

    @Override
    public BulkResponse<AmbulanceDTO> bulkCreate(RequestBulkData<AmbulanceDTO> data) {
        BulkErrorValidEntities<AmbulanceDTO, AmbulanceEntity> entities = requestProcessor().process(data, AmbulanceEntity.class);
        if (entities.failed == null || entities.failed.isEmpty()) {
            ambulanceRepository.saveAll(entities.entityList);
        }
        return responseProcessor().response(entities, AmbulanceDTO.class);

    }
}
