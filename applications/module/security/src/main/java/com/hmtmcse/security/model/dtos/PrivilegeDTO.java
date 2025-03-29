package com.hmtmcse.security.model.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrivilegeDTO {
    private Long created;
    private String name;
    private String urlPattern;
    private String httpMethod;
    private String description;
    private boolean isBasic = true;
}
