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

    @Column
    private String phone;

    @Column(nullable = false)
    private Boolean emailVerified = false;

    @Column(nullable = false)
    private Boolean phoneVerified = false;

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
    private Boolean smsMfaEnabled = false;

    @Column(nullable = false)
    private Boolean emailMfaEnabled = false;

    @Column
    private String preferredMfaFactor;

    @Column(nullable = false)
    private Boolean accountEnabled = true;

    @Column(nullable = false)
    private int tokenVersion = 0;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        final Set<GrantedAuthority> authorities = new LinkedHashSet<>();
        role.forEach(userRole -> {
            if (userRole == null || userRole.getName() == null) {
                return;
            }
            // Keep role projection for legacy guards while migrating to fine-grained authorities.
            authorities.add(new SimpleGrantedAuthority("ROLE_" + userRole.getName()));
            userRole.getPrivilege().forEach(privilege -> {
                if (privilege != null && privilege.getName() != null) {
                    authorities.add(new SimpleGrantedAuthority(privilege.getName()));
                }
            });
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
        return Boolean.TRUE.equals(accountEnabled);
    }

    public Boolean getSmsMfaEnabled() {
        return smsMfaEnabled;
    }

    public void setSmsMfaEnabled(final Boolean smsMfaEnabled) {
        this.smsMfaEnabled = smsMfaEnabled;
    }

    public Boolean getEmailMfaEnabled() {
        return emailMfaEnabled;
    }

    public void setEmailMfaEnabled(final Boolean emailMfaEnabled) {
        this.emailMfaEnabled = emailMfaEnabled;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(final Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public Boolean getPhoneVerified() {
        return phoneVerified;
    }

    public void setPhoneVerified(final Boolean phoneVerified) {
        this.phoneVerified = phoneVerified;
    }

    public String getPreferredMfaFactor() {
        return preferredMfaFactor;
    }

    public void setPreferredMfaFactor(final String preferredMfaFactor) {
        this.preferredMfaFactor = preferredMfaFactor;
    }

    public Boolean getAccountEnabled() {
        return accountEnabled;
    }

    public void setAccountEnabled(final Boolean accountEnabled) {
        this.accountEnabled = accountEnabled;
    }
}
