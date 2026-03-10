package com.security.base.users;

import com.problemfighter.java.oc.annotation.DataMappingInfo;
import com.problemfighter.pfspring.restapi.inter.model.RestDTO;
import com.security.base.core.BaseDTO;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

import lombok.Getter;
import lombok.Setter;


@DataMappingInfo(customProcessor = UsersInterceptor.class)
@Getter
@Setter
public class UsersDTO extends BaseDTO implements RestDTO {

    @Size(max = 255)
    private String name;

    @Size(max = 255)
    private String email;

    @NotNull
    @Size(max = 255)
    @UsersUsernameUnique
    private String username;

    @NotNull
    @Size(max = 255)
    private String password;

    private List<Long> role;

    private Boolean twoFactorEnabled;

    private String totpSecret;

}
