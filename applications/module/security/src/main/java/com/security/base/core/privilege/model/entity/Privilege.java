package com.security.base.core.privilege.model.entity;

import com.security.base.core.BaseEntity;
import com.security.base.core.role.model.entity.Role;
import com.security.base.core.urls.model.entity.Url;
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
public class Privilege extends BaseEntity implements RestEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @ManyToMany(mappedBy = "privilege")
    private Set<Role> role = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "PrivilegeUrl",
            joinColumns = @JoinColumn(name = "privilegeId"),
            inverseJoinColumns = @JoinColumn(name = "urlId")
    )
    private Set<Url> urls = new HashSet<>();

    public Privilege() {
        super();
    }

    public Privilege(String name) {
        super();
        this.name = name;
    }
}
