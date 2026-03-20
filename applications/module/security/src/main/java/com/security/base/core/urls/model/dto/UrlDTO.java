package com.security.base.core.urls.model.dto;

import com.problemfighter.java.oc.annotation.DataMappingInfo;
import com.problemfighter.pfspring.restapi.inter.model.RestDTO;
import com.security.base.core.BaseDTO;
import com.security.base.core.urls.model.mapper.UrlInterceptor;
import com.security.base.core.urls.UrlsEndpointUnique;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@DataMappingInfo(customProcessor = UrlInterceptor.class)
@Getter
@Setter
public class UrlDTO extends BaseDTO implements RestDTO {

    @NotNull
    @Size(max = 255)
    @UrlsEndpointUnique
    private String endpoint;

    @NotNull
    @Size(max = 255)
    private String method;

    @NotNull
    private List<Long> privileges;

}
