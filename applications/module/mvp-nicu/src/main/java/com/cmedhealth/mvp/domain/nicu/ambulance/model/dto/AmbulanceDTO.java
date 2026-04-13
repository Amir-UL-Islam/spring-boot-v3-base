package com.cmedhealth.mvp.domain.nicu.ambulance.model.dto;

import com.hmtmcse.module.dto.BaseDTO;
import com.problemfighter.java.oc.annotation.DataMappingInfo;
import com.problemfighter.pfspring.restapi.inter.model.RestDTO;
import com.cmedhealth.mvp.domain.nicu.ambulance.model.AmbulanceOwnerType;
import com.cmedhealth.mvp.domain.nicu.ambulance.model.mapper.AmbulanceInterceptor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
@DataMappingInfo(customProcessor = AmbulanceInterceptor.class)
public class AmbulanceDTO extends BaseDTO implements RestDTO {
    private Long ownerId;

    private AmbulanceOwnerType ownerType;

    private String contactNumber;

    private String operationArea;

    private Boolean active;

    @EqualsAndHashCode.Exclude
    private Set<AmbulanceVehicleDTO> ambulanceVehicles = new HashSet<>();


}
