package com.hmtmcse.security.model.entites;

import com.hmtmcse.security.model.enums.Permissions;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Setter
@Getter
@Entity
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Temporal(value = TemporalType.TIMESTAMP)
    private Date created;

    private String name;

    private String description;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @Fetch(FetchMode.SUBSELECT)
    @JoinTable(
            name = "role_privilege",
            joinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "privilege_id", referencedColumnName = "id")
    )
    private List<Privilege> privileges = new ArrayList<>();

    private boolean restricted = true;

    public Role() {
    }

    public boolean isAdmin() {
        return privileges != null && privileges.stream()
                .anyMatch(privilege -> Permissions.ADMINISTRATION.getLabel().equals(privilege.getName()));
    }

    public boolean isBasic() {
        return privileges != null && privileges.stream()
                .anyMatch(privilege -> Permissions.BASIC.getLabel().equals(privilege.getName()));
    }

    public boolean isSameAs(Role role) {
        return role != null && this.getId().equals(role.getId());
    }

    public boolean hasPrivilege(Long privilegeId) {
        return privileges != null && privileges.stream()
                .anyMatch(privilege -> privilegeId.equals(privilege.getId()));
    }


}
