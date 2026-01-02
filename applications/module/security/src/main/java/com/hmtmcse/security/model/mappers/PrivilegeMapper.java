package com.hmtmcse.security.model.mappers;

import com.hmtmcse.security.model.dtos.PrivilegeDTO;
import com.hmtmcse.security.model.entites.Privilege;
import com.problemfighter.pfspring.restapi.inter.CopyInterceptor;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class PrivilegeMapper implements CopyInterceptor<Privilege, PrivilegeDTO, PrivilegeDTO> {
    public Privilege map(PrivilegeDTO dto) {
        Privilege entity = new Privilege();
        entity.setCreated(new Date(dto.getCreated() == null || dto.getCreated() == 0 ? System.currentTimeMillis() : dto.getCreated()));
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setHttpMethod(dto.getHttpMethod());
        entity.setUrlPattern(dto.getUrlPattern());
        entity.setBasic(dto.isBasic());
        return entity;
    }

    public PrivilegeDTO map(Privilege entity) {
        PrivilegeDTO dto = new PrivilegeDTO();
        dto.setHttpMethod(entity.getHttpMethod());
        dto.setUrlPattern(entity.getUrlPattern());
        dto.setCreated(entity.getCreated().getTime());
        dto.setDescription(entity.getDescription());
        dto.setName(entity.getName());
        dto.setBasic(entity.isBasic());
        return dto;
    }

    @Override
    public void meAsSrc(PrivilegeDTO source, Privilege destination) {

    }

    @Override
    public void meAsDst(Privilege source, PrivilegeDTO destination) {

    }
}
