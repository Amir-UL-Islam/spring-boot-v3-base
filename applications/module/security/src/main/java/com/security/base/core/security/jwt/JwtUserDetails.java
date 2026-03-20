package com.security.base.core.security.jwt;

import java.util.Collection;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;


/**
 * Extension of the Spring Security User class to store additional data.
 */
@Getter
public class JwtUserDetails extends User {

    private final Long id;

    public JwtUserDetails(
            final Long id,
            final String username,
            final String hash,
            final Collection<? extends GrantedAuthority> authorities
    ) {
        super(username, hash, authorities);
        this.id = id;
    }

}
