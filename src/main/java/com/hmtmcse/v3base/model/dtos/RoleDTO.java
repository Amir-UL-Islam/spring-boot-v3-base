package com.hmtmcse.v3base.model.dtos;

import lombok.Data;

import java.util.List;

@Data
public class RoleDTO {
    private Long created;

    private String name;

    private String description;

    private List<Long> privilegeIds;

    private boolean restricted = true;
}
