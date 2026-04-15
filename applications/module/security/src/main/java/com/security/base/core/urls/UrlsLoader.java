package com.security.base.core.urls;

import com.security.base.core.privilege.model.entity.Privilege;
import com.security.base.core.privilege.repository.PrivilegeRepository;
import com.security.base.core.security.oauth.PermissionCodes;
import com.security.base.core.urls.model.entity.Url;
import com.security.base.core.urls.repository.UrlsRepository;
import jakarta.transaction.Transactional;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


@Component
@Order(2)
@Slf4j
public class UrlsLoader implements ApplicationRunner {

    private final UrlsRepository urlsRepository;
    private final PrivilegeRepository privilegeRepository;

    public UrlsLoader(final UrlsRepository urlsRepository,
                      final PrivilegeRepository privilegeRepository) {
        this.urlsRepository = urlsRepository;
        this.privilegeRepository = privilegeRepository;
    }

    @Override
    @Transactional
    public void run(final ApplicationArguments args) {
        if (urlsRepository.count() != 0) {
            return;
        }
        log.info("initializing urls");

        seedMatrixUrls();
    }

    private void seedMatrixUrls() {
        grant(PermissionCodes.MATRIX_READ,
                entry("/api/me/permissions", methods("GET")));

        grant(PermissionCodes.USER_READ,
                entry("/api/user", methods("GET")),
                entry("/api/user/{id}", methods("GET")));
        grant(PermissionCodes.USER_CREATE,
                entry("/api/user", methods("POST")));
        grant(PermissionCodes.USER_UPDATE,
                entry("/api/user/{id}", methods("PUT")),
                entry("/api/user/logout-all-devices", methods("POST")));
        grant(PermissionCodes.USER_DELETE,
                entry("/api/user/{id}", methods("DELETE")));

        grant(PermissionCodes.ROLE_READ,
                entry("/api/roles", methods("GET")),
                entry("/api/roles/{id}", methods("GET")),
                entry("/api/user/roleValues", methods("GET")));
        grant(PermissionCodes.ROLE_CREATE,
                entry("/api/roles", methods("POST")));
        grant(PermissionCodes.ROLE_UPDATE,
                entry("/api/roles/{id}", methods("PUT")));
        grant(PermissionCodes.ROLE_DELETE,
                entry("/api/roles/{id}", methods("DELETE")));

        grant(PermissionCodes.PRIVILEGE_READ,
                entry("/api/privileges", methods("GET")),
                entry("/api/privileges/{id}", methods("GET")),
                entry("/api/url/privilegeValues", methods("GET")),
                entry("/api/roles/privilegeValues", methods("GET")));
        grant(PermissionCodes.PRIVILEGE_CREATE,
                entry("/api/privileges", methods("POST")));
        grant(PermissionCodes.PRIVILEGE_UPDATE,
                entry("/api/privileges/{id}", methods("PUT")));
        grant(PermissionCodes.PRIVILEGE_DELETE,
                entry("/api/privileges/{id}", methods("DELETE")));
        grant(PermissionCodes.PRIVILEGE_ASSIGN,
                entry("/api/privileges/assign/url", methods("PATCH")),
                entry("/api/privileges/remove/assess/url", methods("PATCH")));

        grant(PermissionCodes.URL_READ,
                entry("/api/url", methods("GET")),
                entry("/api/url/{id}", methods("GET")),
                entry("/api/url/privilege/{id}", methods("GET")));
        grant(PermissionCodes.URL_CREATE,
                entry("/api/url", methods("POST")));
        grant(PermissionCodes.URL_UPDATE,
                entry("/api/url/{id}", methods("PUT")));
        grant(PermissionCodes.URL_DELETE,
                entry("/api/url/{id}", methods("DELETE")));

        grant(PermissionCodes.MODULE_READ,
                entry("/api/v1/modules", methods("GET")),
                entry("/api/v1/modules/{moduleId}", methods("GET")),
                entry("/api/v1/modules/active", methods("GET")));
        grant(PermissionCodes.MODULE_MANAGE,
                entry("/api/v1/modules/{moduleId}/enable", methods("POST")),
                entry("/api/v1/modules/{moduleId}/disable", methods("POST")),
                entry("/api/v1/modules/refresh", methods("POST")));

        grant(PermissionCodes.AMBULANCE_CREATE,
                entry("/api/v1/ambulance", methods("POST")));

        // Keep legacy compatibility for endpoints that rely on generic authenticated user access.
        grant(PermissionCodes.USER,
                entry("/api/2fa/setup", methods("POST")),
                entry("/api/2fa/activate", methods("POST")),
                entry("/api/2fa/disable", methods("POST")));
    }

    @SafeVarargs
    private void grant(final String privilegeName, final Map.Entry<String, String[]>... endpoints) {
        final Privilege privilege = privilegeRepository.findByNameIgnoreCase(privilegeName).orElse(null);
        if (privilege == null) {
            log.warn("privilege {} missing, skipping url mapping", privilegeName);
            return;
        }
        for (final Map.Entry<String, String[]> endpoint : endpoints) {
            addUrls(endpoint.getKey(), endpoint.getValue(), privilege);
        }
    }

    private void addUrls(final String endpoint, final String[] methods, final Privilege privilege) {
        for (final String method : methods) {
            if (urlsRepository.existsByEndpointIgnoreCaseAndMethodIgnoreCase(endpoint, method)) {
                continue;
            }
            final Url url = new Url();
            url.setEndpoint(endpoint);
            url.setMethod(method);
            privilege.getUrls().add(url);
            url.getPrivileges().add(privilege);
            urlsRepository.save(url);           // persist Url first to get an ID
            privilegeRepository.save(privilege);
        }
    }

    private static String[] methods(final String... methods) {
        return methods;
    }

    private static Map.Entry<String, String[]> entry(final String key, final String[] value) {
        return Map.entry(key, value);
    }
}

