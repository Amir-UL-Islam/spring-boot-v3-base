package com.security.base.core.urls.model.entity;

import com.hmtmcse.module.entity.BaseEntity;
import com.security.base.core.privilege.model.entity.Privilege;
import com.problemfighter.pfspring.restapi.inter.model.RestEntity;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;


@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Url extends BaseEntity implements RestEntity {

    @Column(nullable = false)
    private String endpoint;

    @Column(nullable = false)
    private String method;

    @ManyToMany(mappedBy = "urls")
    private Set<Privilege> privileges = new HashSet<>();

}
