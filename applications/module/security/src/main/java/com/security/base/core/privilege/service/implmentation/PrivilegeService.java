package com.security.base.core.privilege.service.implmentation;

import com.security.base.core.privilege.model.dto.PrivilegeDTO;
import com.security.base.core.privilege.model.entity.Privilege;
import com.security.base.core.privilege.repository.PrivilegeRepository;
import com.security.base.events.BeforeDeletePrivilege;
import com.security.base.core.urls.model.entity.Url;
import com.security.base.core.urls.repository.UrlsRepository;
import com.security.base.util.CustomCollectors;
import com.security.base.util.NotFoundException;
import com.problemfighter.pfspring.restapi.rr.RequestResponse;

import java.util.*;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class PrivilegeService implements RequestResponse {

    private final UrlsRepository urlsRepository;
    private final PrivilegeRepository privilegeRepository;
    private final ApplicationEventPublisher publisher;


    public List<PrivilegeDTO> findAll() {
        final List<Privilege> privileges = privilegeRepository.findAll(Sort.by("id"));
        return responseProcessor().entityToDTO(privileges, PrivilegeDTO.class);
    }

    public PrivilegeDTO get(final Long id) {
        return privilegeRepository.findById(id)
                .map(privilege -> responseProcessor().entityToDTO(privilege, PrivilegeDTO.class))
                .orElseThrow(NotFoundException::new);
    }

    public Long create(final PrivilegeDTO privilegeDTO) {
        final Privilege privilege = new Privilege();
        requestProcessor().process(privilegeDTO, privilege);
        return privilegeRepository.save(privilege).getId();
    }

    public void update(final Long id, final PrivilegeDTO privilegeDTO) {
        final Privilege privilege = privilegeRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        requestProcessor().process(privilegeDTO, privilege);
        privilegeRepository.save(privilege);
    }

    public void delete(final Long id) {
        final Privilege privilege = privilegeRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        publisher.publishEvent(new BeforeDeletePrivilege(id));
        privilegeRepository.delete(privilege);
    }


    public boolean nameExists(final String name) {
        return privilegeRepository.existsByNameIgnoreCase(name);
    }

    public Map<Long, String> getPrivilegeValues() {
        return privilegeRepository.findAll(Sort.by("id"))
                .stream()
                .collect(CustomCollectors.toSortedMap(Privilege::getId, Privilege::getName));
    }

    public void assignUrl(Long privilegeId, Long urlId) {
        Privilege privilege = privilegeRepository.findById(privilegeId).orElseThrow();
        Url url = urlsRepository.findById(urlId).orElseThrow();

        privilege.getUrls().add(url);
        urlsRepository.save(url);           // persist Url first to get an ID
        privilegeRepository.save(privilege);
    }

    public void removeAssignUrl(Long privilegeId, Long urlId) {
        Privilege privilege = privilegeRepository.findById(privilegeId).orElseThrow();
        Url url = urlsRepository.findById(urlId).orElseThrow();

        privilege.getUrls().remove(url);
        urlsRepository.save(url);           // persist Url first to get an ID
        privilegeRepository.save(privilege);
    }

    public boolean hasPermission(Authentication authentication, HttpServletRequest request) {
        // Extract the current URL pattern and HTTP method
        String urlPattern = request.getRequestURI();


        // Policy-first applies to API routes. Static assets and non-API routes are handled elsewhere.
        if (!urlPattern.startsWith("/api")) {
            return true;
        }

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String httpMethod = request.getMethod();

        // Find permissions for this URL and method
        List<Privilege> permissions = privilegeRepository.findByUrlPatternAndHttpMethod(urlPattern, httpMethod);

        // Policy-first mode: explicit URL policy is required for every API endpoint.
        if (permissions.isEmpty()) {
            return false;
        }

        // Get user authorities
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        // Check if a user has a required role
        return permissions.stream()
                .anyMatch(permission ->
                        authorities.stream()
                                .anyMatch(authority ->
                                        Objects.equals(authority.getAuthority(), permission.getName())
                                )
                );
    }
}
