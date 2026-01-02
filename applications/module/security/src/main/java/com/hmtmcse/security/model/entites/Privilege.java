package com.hmtmcse.security.model.entites;

import com.problemfighter.pfspring.restapi.inter.model.RestEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
@Entity
public class Privilege implements RestEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Temporal(value = TemporalType.TIMESTAMP)
    private Date created;

    private String name;

    private String description;

    private String urlPattern;

    private String httpMethod; // no need

    private boolean isBasic = true;
}
