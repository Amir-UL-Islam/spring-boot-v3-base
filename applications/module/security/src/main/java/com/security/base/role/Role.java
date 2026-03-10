package com.security.base.role;

import com.security.base.core.BaseEntity;
import com.security.base.privilege.Privilege;
import com.security.base.users.Users;
import com.problemfighter.pfspring.restapi.inter.model.RestEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;

import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;


@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Role extends BaseEntity implements RestEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String description;

    @ManyToMany(mappedBy = "role")
    private Set<Users> user = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "RolePrivilege",
            joinColumns = @JoinColumn(name = "roleId"),
            inverseJoinColumns = @JoinColumn(name = "privilegeId")
    )
    private Set<Privilege> privilege = new HashSet<>();



}
