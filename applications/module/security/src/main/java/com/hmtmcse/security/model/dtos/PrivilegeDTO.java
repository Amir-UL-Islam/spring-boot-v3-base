package com.hmtmcse.security.model.dtos;

import com.problemfighter.pfspring.restapi.inter.model.RestDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrivilegeDTO implements RestDTO {
    private Long created;
    private String name;
    private String urlPattern;
    private String httpMethod;
    private String description;
    private boolean isBasic = true;
}
