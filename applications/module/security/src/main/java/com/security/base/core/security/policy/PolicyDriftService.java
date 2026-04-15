package com.security.base.core.security.policy;

import com.security.base.core.security.oauth.PermissionCodes;
import com.security.base.core.urls.model.entity.Url;
import com.security.base.core.urls.repository.UrlsRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;


@Service
@RequiredArgsConstructor
public class PolicyDriftService {

    private static final Pattern HAS_AUTHORITY_PATTERN =
            Pattern.compile("hasAuthority\\('([^']+)'\\)");
    private static final Pattern HAS_ANY_AUTHORITY_PATTERN =
            Pattern.compile("hasAnyAuthority\\(([^)]+)\\)");
    private static final Pattern SINGLE_QUOTED = Pattern.compile("'([^']+)'");

    private final RequestMappingHandlerMapping handlerMapping;
    private final UrlsRepository urlsRepository;

    @Transactional(readOnly = true)
    public PolicyDriftReport analyzeDrift() {
        final List<PolicyDriftIssue> issues = new ArrayList<>();

        for (final var entry : handlerMapping.getHandlerMethods().entrySet()) {
            final RequestMappingInfo mappingInfo = entry.getKey();
            final HandlerMethod handlerMethod = entry.getValue();
            final Set<String> authorities = extractAuthorities(handlerMethod);
            if (authorities.isEmpty()) {
                continue;
            }

            final Set<String> patterns = mappingInfo.getPatternValues();
            final Set<String> methods = mappingInfo.getMethodsCondition().getMethods().isEmpty()
                    ? Set.of("GET")
                    : mappingInfo.getMethodsCondition().getMethods().stream().map(Enum::name).collect(java.util.stream.Collectors.toSet());

            for (final String pattern : patterns) {
                if (!pattern.startsWith("/api")) {
                    continue;
                }
                for (final String method : methods) {
                    checkAuthorityConventions(issues, pattern, method, authorities);
                    checkUrlPolicyCoverage(issues, pattern, method, authorities);
                }
            }
        }

        return new PolicyDriftReport(Instant.now(), issues.size(), issues);
    }

    private void checkAuthorityConventions(final List<PolicyDriftIssue> issues,
                                           final String endpoint,
                                           final String method,
                                           final Set<String> authorities) {
        authorities.forEach(authority -> {
            if (!PermissionCodes.isSupportedPermissionCode(authority)) {
                issues.add(new PolicyDriftIssue(
                        "ERROR",
                        "INVALID_AUTHORITY_FORMAT",
                        endpoint,
                        method,
                        authority,
                        "Authority must follow resource:action convention or be legacy ADMIN/USER"
                ));
                return;
            }
            if (!PermissionCodes.isKnownCode(authority) && PermissionCodes.isMatrixCode(authority)) {
                issues.add(new PolicyDriftIssue(
                        "WARN",
                        "UNKNOWN_AUTHORITY_CODE",
                        endpoint,
                        method,
                        authority,
                        "Authority follows convention but is not declared in PermissionCodes"
                ));
            }
        });
    }

    private void checkUrlPolicyCoverage(final List<PolicyDriftIssue> issues,
                                        final String endpoint,
                                        final String method,
                                        final Set<String> authorities) {
        final Url urlPolicy = urlsRepository.findByEndpointIgnoreCaseAndMethodIgnoreCase(endpoint, method).orElse(null);
        if (urlPolicy == null) {
            authorities.forEach(authority -> issues.add(new PolicyDriftIssue(
                    "ERROR",
                    "URL_POLICY_MISSING",
                    endpoint,
                    method,
                    authority,
                    "No URL policy mapping exists for this endpoint/method"
            )));
            return;
        }

        final Set<String> mappedPrivilegeNames = new LinkedHashSet<>();
        urlPolicy.getPrivileges().forEach(privilege -> {
            if (privilege != null && privilege.getName() != null) {
                mappedPrivilegeNames.add(privilege.getName());
            }
        });

        authorities.stream()
                .filter(authority -> !mappedPrivilegeNames.contains(authority))
                .forEach(authority -> issues.add(new PolicyDriftIssue(
                        "ERROR",
                        "URL_POLICY_PRIVILEGE_GAP",
                        endpoint,
                        method,
                        authority,
                        "Endpoint policy exists but does not include required authority"
                )));
    }

    private Set<String> extractAuthorities(final HandlerMethod handlerMethod) {
        final Set<String> authorities = new LinkedHashSet<>();

        final PreAuthorize classAnnotation = handlerMethod.getBeanType().getAnnotation(PreAuthorize.class);
        final PreAuthorize methodAnnotation = handlerMethod.getMethodAnnotation(PreAuthorize.class);

        if (classAnnotation != null) {
            extractAuthoritiesFromExpression(classAnnotation.value(), authorities);
        }
        if (methodAnnotation != null) {
            extractAuthoritiesFromExpression(methodAnnotation.value(), authorities);
        }
        return authorities;
    }

    private void extractAuthoritiesFromExpression(final String expression, final Set<String> output) {
        final Matcher hasAuthorityMatcher = HAS_AUTHORITY_PATTERN.matcher(expression);
        while (hasAuthorityMatcher.find()) {
            output.add(hasAuthorityMatcher.group(1));
        }

        final Matcher hasAnyMatcher = HAS_ANY_AUTHORITY_PATTERN.matcher(expression);
        while (hasAnyMatcher.find()) {
            final Matcher quoted = SINGLE_QUOTED.matcher(hasAnyMatcher.group(1));
            while (quoted.find()) {
                output.add(quoted.group(1));
            }
        }
    }
}

