package com.hmtmcse.v3base.services;

import com.hmtmcse.v3base.exceptions.NotFoundException;
import com.hmtmcse.v3base.model.entites.Privilege;
import com.hmtmcse.v3base.repositories.PrivilegeRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PrivilegeService {

    private final PrivilegeRepository privilegeRepository;
    private static final Logger logger = LoggerFactory.getLogger(PrivilegeService.class);
    private final static String[] BASIC_URLs = {"/api/v1/users/welcome", "/api/v1/users/test-user-role"};

    public Privilege findById(Long id) throws NotFoundException {
        return privilegeRepository.findById(id).orElseThrow(() -> new NotFoundException("Privilege not found!"));
    }

    public boolean hasPermission(Authentication authentication, HttpServletRequest request) {
        // Extract the current URL pattern and HTTP method
        String urlPattern = request.getRequestURI();

        String httpMethod = request.getMethod();

        // Find permissions for this URL and method
        List<Privilege> permissions = privilegeRepository.findByUrlPatternAndHttpMethod(urlPattern, httpMethod);

        // If no specific permissions, deny access
        if (permissions.isEmpty()) {
            return false;
        }

        // Get user authorities
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        // Check if user has required role
        return permissions.stream()
                .anyMatch(permission ->
                        authorities.stream()
                                .anyMatch(authority ->
                                        authority.getAuthority().equals(permission.getName())
                                )
                );
    }


    public Privilege find(Long id) {
        return privilegeRepository.findById(id).orElse(null);
    }

    public Privilege save(Privilege permission) {
        return privilegeRepository.save(permission);
    }

    public List<Privilege> getBasicPrivileges() {
        List<Privilege> basicPrivileges = privilegeRepository.getBasicPrivileges();
        if (basicPrivileges.isEmpty()) {
            return createBasicURLs();
        }
        return basicPrivileges;
    }

    public List<Privilege> createBasicURLs() {
        List<Privilege> basicPrivileges = new ArrayList<>();
        for (String url : BASIC_URLs) {
            Privilege entity = new Privilege();
            entity.setCreated(new Date());
            entity.setName("Basic");
            entity.setDescription("Basic URL");
            entity.setUrlPattern(url);
            entity.setHttpMethod("GET");
            entity.setBasic(true);
            basicPrivileges.add(entity);
        }
        return privilegeRepository.saveAll(basicPrivileges);
    }
}
