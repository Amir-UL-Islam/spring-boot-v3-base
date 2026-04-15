package com.security.base.core.security.filters;


import com.security.base.core.privilege.service.implmentation.PrivilegeService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ACLFilter extends OncePerRequestFilter {

    private final PrivilegeService permissionService;
    private static final List<String> PUBLIC_PATHS = List.of(
            "/register",
            "/authenticate",
            "/authenticateGoogle",
            "/refresh-token",
            "/oauth/token"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String urlPattern = request.getRequestURI();

        // Policy-first ACL is only for API routes; keep static/public assets accessible.
        if (!urlPattern.startsWith("/api")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (PUBLIC_PATHS.stream().anyMatch(urlPattern::startsWith)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (authentication == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized");
            return;
        }

        if (!permissionService.hasPermission(authentication, request)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("Access Denied: You do not have permission to access this resource");
            return;
        }
        filterChain.doFilter(request, response);
    }
}