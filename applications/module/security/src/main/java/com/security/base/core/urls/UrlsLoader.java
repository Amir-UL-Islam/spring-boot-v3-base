package com.security.base.core.urls;

import com.security.base.core.privilege.model.entity.Privilege;
import com.security.base.core.privilege.repository.PrivilegeRepository;
import com.security.base.core.urls.model.entity.Url;
import com.security.base.core.urls.repository.UrlsRepository;
import jakarta.transaction.Transactional;

import java.util.Map;
import java.util.Optional;

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

        final Optional<Privilege> adminPrivilege = privilegeRepository.findByNameIgnoreCase("ADMIN");
        final Optional<Privilege> userPrivilege = privilegeRepository.findByNameIgnoreCase("USER");
        if (adminPrivilege.isEmpty() || userPrivilege.isEmpty()) {
            log.warn("privileges missing, skipping url seeding");
            return;
        }

        seedAdminUrls(adminPrivilege.get());
        seedUserUrls(userPrivilege.get());
    }

    private void seedAdminUrls(final Privilege privilege) {
        Map<String, String[]> adminEndpoints = Map.ofEntries(
                entry("/authenticate", methods("POST")),
                entry("/authenticateGoogle", methods("POST")),
                entry("/register", methods("POST")),

                entry("/api/roles", methods("GET", "POST", "PUT", "PATCH", "DELETE")),
                entry("/api/roles/privilegeValues", methods("GET")),
                entry("/api/roles/{id}", methods("GET", "PUT", "DELETE")),

                entry("/api/privileges", methods("GET", "POST", "PUT", "PATCH", "DELETE")),
                entry("/api/privileges/{id}", methods("GET", "PUT", "DELETE")),

                entry("/api/url", methods("GET", "POST", "PUT", "PATCH", "DELETE")),
                entry("/api/url/privilegeValues", methods("GET")),
                entry("/api/url/{id}", methods("GET", "PUT", "DELETE")),

                entry("/api/user", methods("GET", "POST", "PUT", "PATCH", "DELETE")),
                entry("/api/user/roleValues", methods("GET")),
                entry("/api/user/{id}", methods("GET", "PUT", "DELETE")),
                entry("/api/2fa/setup", methods("POST")),
                entry("/api/2fa/activate", methods("POST")),
                entry("/api/2fa/disable", methods("POST"))
        );

        adminEndpoints.forEach((endpoint, methods) -> addUrls(endpoint, methods, privilege));
    }

    private void seedUserUrls(final Privilege privilege) {
        Map<String, String[]> userEndpoints = Map.of(
                "/api/user", methods("GET"),
                "/api/user/{id}", methods("GET"),
                "/api/2fa/setup", methods("POST"),
                "/api/2fa/activate", methods("POST"),
                "/api/2fa/disable", methods("POST")
        );
        userEndpoints.forEach((endpoint, methods) -> addUrls(endpoint, methods, privilege));
    }

    private void addUrls(final String endpoint, final String[] methods, final Privilege privilege) {
        for (final String method : methods) {
            final Url url = new Url();
            url.setEndpoint(endpoint);
            url.setMethod(method);
            privilege.getUrls().add(url);
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

