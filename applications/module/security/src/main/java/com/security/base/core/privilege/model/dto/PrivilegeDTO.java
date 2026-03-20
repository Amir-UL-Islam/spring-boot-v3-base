package com.security.base.core.privilege.model.dto;

import com.problemfighter.java.oc.annotation.DataMappingInfo;
import com.problemfighter.pfspring.restapi.inter.model.RestDTO;
import com.security.base.core.BaseDTO;
import com.security.base.core.privilege.model.mapper.PrivilegeInterceptor;
import com.security.base.core.privilege.PrivilegeNameUnique;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;


@DataMappingInfo(customProcessor = PrivilegeInterceptor.class)
@Getter
@Setter
public class PrivilegeDTO extends BaseDTO implements RestDTO {

    @NotNull
    @Size(max = 255)
    @PrivilegeNameUnique
    private String name;

}
