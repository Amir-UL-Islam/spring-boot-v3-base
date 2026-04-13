package com.cmedhealth.mvp.domain.nicu.ambulance.controller;

import com.problemfighter.pfspring.restapi.inter.RestApiAction;
import com.problemfighter.pfspring.restapi.rr.request.RequestData;
import com.problemfighter.pfspring.restapi.rr.response.DetailsResponse;
import com.cmedhealth.mvp.domain.nicu.ambulance.model.dto.AmbulanceDTO;
import com.cmedhealth.mvp.domain.nicu.ambulance.service.AmbulanceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Ambulance Management", description = "APIs for managing ambulance registrations and details")
@RequestMapping("/api/v1")
public class AmbulanceController implements RestApiAction<AmbulanceDTO, AmbulanceDTO, AmbulanceDTO> {
    private final AmbulanceService ambulanceService;

    @PostMapping("/ambulance")
    public DetailsResponse<AmbulanceDTO> register(@RequestBody @Valid RequestData<AmbulanceDTO> data) {
        return ambulanceService.save(data);
    }
}
