package com.flyway.security.filter;

import com.flyway.auth.domain.AuthStatus;
import com.flyway.security.principal.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class OnboardingAccessFilter extends OncePerRequestFilter {

    private static final String[] ALLOWED_PREFIXES = {
            "/signup",
            "/auth/",
            "/logout",
            "/resources/",
            "/css/",
            "/js/",
            "/images/",
            "/favicon.ico",
            "/error",
            "/api/auth/",
            "/api/public/"
    };

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!isOnboardingUser(authentication)) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = resolvePath(request);
        if (isAllowedPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (path.startsWith("/api/")) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Onboarding required");
            return;
        }

        response.sendRedirect(request.getContextPath() + "/signup");
    }

    private boolean isOnboardingUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CustomUserDetails)) {
            return false;
        }
        CustomUserDetails userDetails = (CustomUserDetails) principal;
        return AuthStatus.ONBOARDING.equals(userDetails.getUser().getStatus());
    }

    private String resolvePath(HttpServletRequest request) {
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && path.startsWith(contextPath)) {
            return path.substring(contextPath.length());
        }
        return path;
    }

    private boolean isAllowedPath(String path) {
        for (String prefix : ALLOWED_PREFIXES) {
            if (path.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}
