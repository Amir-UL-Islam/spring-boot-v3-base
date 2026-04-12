package com.security.base.core.role.model.dto;

import com.problemfighter.java.oc.annotation.DataMappingInfo;
import com.problemfighter.pfspring.restapi.inter.model.RestDTO;
import com.hmtmcse.module.dto.BaseDTO;
import com.security.base.core.role.RoleDescriptionUnique;
import com.security.base.core.role.model.mapper.RoleInterceptor;
import com.security.base.core.role.RoleNameUnique;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;
import lombok.Setter;


@DataMappingInfo(customProcessor = RoleInterceptor.class)
@Getter
@Setter
public class RoleDTO extends BaseDTO implements RestDTO {


    @NotNull
    @Size(max = 255)
    @RoleNameUnique
    private String name;

    @NotNull
    @Size(max = 255)
    @RoleDescriptionUnique
    private String description;

    private List<Long> privilege;

}
