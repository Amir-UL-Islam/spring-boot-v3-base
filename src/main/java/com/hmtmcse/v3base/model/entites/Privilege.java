package com.hmtmcse.v3base.model.entites;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
@Entity
public class Privilege {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Temporal(value = TemporalType.TIMESTAMP)
    private Date created;

    private String name;

    private String description;

    private String urlPattern;

    private String httpMethod;

    private boolean isBasic = true;
}
