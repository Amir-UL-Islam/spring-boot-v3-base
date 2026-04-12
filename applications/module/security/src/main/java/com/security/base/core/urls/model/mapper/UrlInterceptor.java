package com.security.base.core.urls.model.mapper;

import com.hmtmcse.module.entity.BaseEntity;
import com.security.base.core.privilege.model.entity.Privilege;
import com.security.base.core.privilege.repository.PrivilegeRepository;
import com.security.base.core.urls.model.dto.UrlDTO;
import com.security.base.core.urls.model.entity.Url;
import com.security.base.util.NotFoundException;
import com.problemfighter.pfspring.restapi.inter.CopyInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;

@Component
@RequiredArgsConstructor
public class UrlInterceptor implements CopyInterceptor<Url, UrlDTO, UrlDTO> {
    private final PrivilegeRepository privilegeRepository;

    @Override
    public void meAsSrc(UrlDTO source, Url destination) {
        // Only handle privileges relationship - endpoint and method are auto-mapped
        destination.setPrivileges(new HashSet<>());
        source.getPrivileges().forEach(id -> {
            final Privilege privilege = privilegeRepository.findById(id)
                    .orElseThrow(NotFoundException::new);
            destination.getPrivileges().add(privilege);
        });
    }

    @Override
    public void meAsDst(Url source, UrlDTO destination) {
        // Only handle privileges relationship - id, endpoint, and method are auto-mapped
        destination.setPrivileges(source.getPrivileges().stream()
                .map(BaseEntity::getId)
                .toList());
    }
}
