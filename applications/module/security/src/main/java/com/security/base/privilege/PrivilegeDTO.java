package com.security.base.privilege;

import com.problemfighter.java.oc.annotation.DataMappingInfo;
import com.problemfighter.pfspring.restapi.inter.model.RestDTO;
import com.security.base.core.BaseDTO;
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
