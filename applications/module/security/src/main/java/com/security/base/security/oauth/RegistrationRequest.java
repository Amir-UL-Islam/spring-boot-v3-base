package com.security.base.security.oauth;

import com.security.base.users.UsersUsernameUnique;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class RegistrationRequest {

    @Size(max = 255)
    private String name;

    @NotNull
    @Size(max = 255)
    @UsersUsernameUnique(message = "{registration.register.taken}")
    private String username;

    @NotNull
    @Size(max = 72)
    private String password;

}
