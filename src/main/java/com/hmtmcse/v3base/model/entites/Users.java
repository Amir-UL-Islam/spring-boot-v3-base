package com.hmtmcse.v3base.model.entites;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
@Entity
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Temporal(value = TemporalType.TIMESTAMP)
    private Date created;

    private String firstName;

    private String lastName;

    private String username;

    private String email;

    private String phone;

    private Byte gender;

    private String password;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {
            CascadeType.PERSIST,
            CascadeType.MERGE
    })
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id")
    )
    private Set<Role> roles = new HashSet<>();

    private boolean enabled = true;

    private boolean accountNonExpired = true;

    private boolean accountNonLocked = true;

    private boolean credentialsNonExpired = true;

    public Users() {
    }

    public Users(UserAuth auth) {
        if (auth == null) throw new IllegalArgumentException("User can not be null!");
        this.setId(auth.getId());
        this.firstName = auth.getFirstName();
        this.lastName = auth.getLastName();
        this.username = auth.getUsername();
        this.password = auth.getPassword();
        this.phone = auth.getPhone();
        this.email = auth.getEmail();
        this.enabled = auth.isEnabled();
        this.roles = auth.getRoles();
        this.accountNonExpired = auth.isAccountNonExpired();
        this.accountNonLocked = auth.isAccountNonLocked();
        this.credentialsNonExpired = auth.isCredentialsNonExpired();
    }

    public void grantRole(Role role) {
        if (this.roles == null)
            this.roles = new HashSet<>();
        // check if user already has that role
        if (!hasRole(role) && !role.isAdmin())
            this.roles.add(role);
    }

    public boolean canLogin() {
        return this.enabled
                && this.accountNonExpired
                && this.accountNonLocked
                && this.credentialsNonExpired;
    }

    public boolean hasRole(Role role) {
        return this.roles != null && this.roles.stream().anyMatch(r -> r.isSameAs(role));
    }

    public boolean isAdmin() {
        return this.roles != null &&
                this.roles.stream().anyMatch(Role::isAdmin);
    }

    public List<Long> getRoleIdList() {
        List<Long> roleIds = new ArrayList<>();
        if (this.roles != null) {
            for (Role role : this.roles) {
                roleIds.add(role.getId());
            }
        }
        return roleIds;
    }

    public boolean isSameUsername(String username) {
        if (username == null) return false;
        return username.trim().equals(this.username) || ("+88" + username.trim()).equals(this.username);
    }

    public String getName() {
        if (lastName == null) return firstName;
        return firstName + " " + lastName;
    }
}
