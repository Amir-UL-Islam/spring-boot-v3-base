package io.base.domain.nicu.ambulance.model.entity;

import com.hmtmcse.module.entity.BaseEntity;
import com.problemfighter.pfspring.restapi.inter.model.RestEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;


@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "ambulance_vehicle", indexes = {
        @Index(name = "idx_registration_number", columnList = "registrationNumber")
})
public class AmbulanceVehicleEntity extends BaseEntity implements RestEntity {
    private String registrationNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ambulance_id", nullable = false)
    private AmbulanceEntity ambulance;

    private String vehicleName;

    private String pictureUrl;

    private Boolean isNicuFacilityAvailable;

    private String serviceArea;

    private Boolean active;
}
