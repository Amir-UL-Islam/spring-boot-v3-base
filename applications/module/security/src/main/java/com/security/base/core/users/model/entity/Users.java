package com.security.base.core.users.model.entity;

import com.hmtmcse.module.entity.BaseEntity;
import com.security.base.core.role.model.entity.Role;
import com.problemfighter.pfspring.restapi.inter.model.RestEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;


@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Users extends BaseEntity implements UserDetails, RestEntity {
    @Column
    private String name;

    @Column
    private String email;

    @Column(nullable = false, unique = true)
    private String username;

    //    @Column(nullable = false)
    private String password;

    @ManyToMany
    @JoinTable(
            name = "UsersRoles",
            joinColumns = @JoinColumn(name = "usersId"),
            inverseJoinColumns = @JoinColumn(name = "roleId")
    )
    private Set<Role> role = new HashSet<>();

    @Column
    private Boolean twoFactorEnabled = false;

    @Column
    private String totpSecret;

    @Column(nullable = false)
    private int tokenVersion = 0;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        final Set<GrantedAuthority> authorities = new LinkedHashSet<>();
        role.forEach(userRole -> {
            // Keep role projection for legacy guards while migrating to fine-grained authorities.
            authorities.add(new SimpleGrantedAuthority("ROLE_" + userRole.getName()));
            userRole.getPrivilege().forEach(privilege ->
                    authorities.add(new SimpleGrantedAuthority(privilege.getName())));
        });
        return authorities;
    }


    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}
