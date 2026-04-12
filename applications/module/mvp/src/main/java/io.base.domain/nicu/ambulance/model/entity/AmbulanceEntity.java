package io.base.domain.nicu.ambulance.model.entity;

import com.hmtmcse.module.entity.BaseEntity;
import com.problemfighter.pfspring.restapi.inter.model.RestEntity;
import com.security.base.core.users.model.entity.Users;
import io.base.domain.nicu.ambulance.model.AmbulanceOwnerType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "ambulance")
public class AmbulanceEntity extends BaseEntity implements RestEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Users owner;

    private AmbulanceOwnerType ownerType;

    private String contactNumber;

    private String operationArea;

    private Boolean active;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "ambulance")
    private Set<AmbulanceVehicleEntity> ambulanceVehicles = new HashSet<>();

}
