package com.hmtmcse.module.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Getter
@Setter
public class BaseDTO implements Serializable {
    private Long id;
    private OffsetDateTime createDate;
    private OffsetDateTime updateDate;
    private String createdBy;
    private String updatedBy;
    private String uuid;

}
