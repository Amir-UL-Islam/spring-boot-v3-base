package com.hmtmcse.v3base.model.mappers;

import com.hmtmcse.v3base.model.dtos.PrivilegeDTO;
import com.hmtmcse.v3base.model.entites.Privilege;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RequiredArgsConstructor
public class PrivilegeMapper {
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
}
