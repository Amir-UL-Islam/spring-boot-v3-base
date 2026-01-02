package com.hmtmcse.security.model.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Registration {
    @Schema(hidden = true)
    private Long created;

    private String username;
    private String password;
    private String phone;
    private String email;
    private String firstName;
    private String lastName;
    private Byte gender;
}