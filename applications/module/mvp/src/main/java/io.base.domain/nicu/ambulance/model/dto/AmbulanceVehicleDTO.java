package io.base.domain.nicu.ambulance.model.dto;

import com.hmtmcse.module.dto.BaseDTO;
import com.problemfighter.java.oc.annotation.DataMappingInfo;
import com.problemfighter.pfspring.restapi.inter.model.RestDTO;
import io.base.domain.nicu.ambulance.model.mapper.AmbulanceVehicleInterceptor;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;


@EqualsAndHashCode(callSuper = true)
@DataMappingInfo(customProcessor = AmbulanceVehicleInterceptor.class)
@Data
public class AmbulanceVehicleDTO extends BaseDTO implements RestDTO {
    private Long ambulanceId;

    @NotNull
    private String registrationNumber;

    private String vehicleName;

    private String pictureUrl;

    private Boolean isNicuFacilityAvailable;

    private String serviceArea;

    private Boolean active;
}
