package com.hmtmcse.v3base.model.entites;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;


public class UserAuth implements UserDetails {
    @Getter
    @Setter
    private Long id;
    @Getter
    @Setter
    private String firstName;
    @Getter
    @Setter
    private String lastName;
    private final String username;
    private final String password;
    @Getter
    @Setter
    private String phone;
    @Getter
    @Setter
    private String email;

    @Getter
    @Setter
    private Set<Role> roles = new HashSet<>();

    private boolean enabled = true;

    private boolean accountNonExpired = true;

    private boolean accountNonLocked = true;

    private boolean credentialsNonExpired = true;
    @Getter
    @Setter
    private boolean admin = false;


    public UserAuth(Users user) {
        if (user == null) throw new IllegalArgumentException("User can not be null!");
        this.id = user.getId();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.phone = user.getPhone();
        this.email = user.getEmail();
        this.enabled = user.isEnabled();
        this.roles = user.getRoles();
        this.accountNonExpired = user.isAccountNonExpired();
        this.accountNonLocked = user.isAccountNonLocked();
        this.credentialsNonExpired = user.isCredentialsNonExpired();
        this.admin = user.isAdmin();
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (this.roles == null) this.roles = new HashSet<>();
        List<GrantedAuthority> authorityList = new ArrayList<>();
        for (Role role : this.roles) {
            if (role.getPrivileges() == null) continue;
            authorityList.addAll(
                    role.getPrivileges().stream()
                            .map(privilege -> new SimpleGrantedAuthority(privilege.getName()))
                            .toList()
            );
        }
        return authorityList;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return this.accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return this.credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }
}