package com.security.base.urls;

import com.security.base.privilege.Privilege;
import com.security.base.privilege.PrivilegeRepository;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Component
@Transactional
@Order(4)
@Slf4j
public class EndpointSync implements ApplicationRunner {

    private final RequestMappingHandlerMapping handlerMapping;
    private final UrlsRepository urlsRepository;
    private final PrivilegeRepository privilegeRepository;

    public EndpointSync(final RequestMappingHandlerMapping handlerMapping,
                        final UrlsRepository urlsRepository,
                        final PrivilegeRepository privilegeRepository) {
        this.handlerMapping = handlerMapping;
        this.urlsRepository = urlsRepository;
        this.privilegeRepository = privilegeRepository;
    }

    @Override
    public void run(final ApplicationArguments args) {
        final Privilege adminPrivilege = privilegeRepository.findByNameIgnoreCase("ADMIN")
                .orElse(null);
        if (adminPrivilege == null) {
            log.warn("ADMIN privilege missing; skipping endpoint sync");
            return;
        }

        for (RequestMappingInfo info : handlerMapping.getHandlerMethods().keySet()) {
            final Set<String> patterns = info.getPatternValues();
            final Set<String> methods = info.getMethodsCondition().getMethods().isEmpty()
                    ? Set.of("GET")
                    : info.getMethodsCondition().getMethods().stream().map(Enum::name).collect(Collectors.toSet());
            for (String pattern : patterns) {
                if (pattern.startsWith("/error") || pattern.startsWith("/swagger") || pattern.startsWith("/v3")) {
                    continue;
                }
                for (String method : methods) {
                    if (urlsRepository.existsByEndpointIgnoreCaseAndMethodIgnoreCase(pattern, method)) {
                        continue;
                    }
                    final Url url = new Url();
                    url.setEndpoint(pattern);
                    url.setMethod(method);
                    adminPrivilege.getUrls().add(url);
                    url.getPrivileges().add(adminPrivilege);
                    urlsRepository.save(url);
                    privilegeRepository.save(adminPrivilege);
                    log.info("Synced endpoint {} {}", method, pattern);
                }
            }
        }
    }
}