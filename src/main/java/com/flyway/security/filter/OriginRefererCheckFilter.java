package com.flyway.security.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Slf4j
public class OriginRefererCheckFilter extends OncePerRequestFilter {

    private static final Set<String> STATE_CHANGING_METHODS =
            new LinkedHashSet<>(Arrays.asList("POST", "PUT", "PATCH", "DELETE"));

    private final Set<String> allowedOrigins;
    private final List<String> includeBasePaths;
    private final List<String> excludeBasePaths;
    private final Set<String> excludeExactPaths;

    public OriginRefererCheckFilter(
            Collection<String> allowedOrigins,
            Collection<String> includeBasePaths,
            Collection<String> excludeBasePaths,
            Collection<String> excludeExactPaths
    ) {
        this.allowedOrigins = normalizeOrigins(allowedOrigins);
        this.includeBasePaths = normalizePaths(includeBasePaths);
        this.excludeBasePaths = normalizePaths(excludeBasePaths);
        this.excludeExactPaths = new LinkedHashSet<>();
        if (excludeExactPaths != null) {
            for (String p : excludeExactPaths) {
                if (StringUtils.hasText(p)) {
                    this.excludeExactPaths.add(normalizePath(p));
                }
            }
        }
    }

    public static OriginRefererCheckFilter forApi(Collection<String> allowedOrigins) {
        return new OriginRefererCheckFilter(
                allowedOrigins,
                Arrays.asList("/api"),
                null,
                null
        );
    }

    public static OriginRefererCheckFilter forWeb(Collection<String> allowedOrigins) {
        return new OriginRefererCheckFilter(
                allowedOrigins,
                Arrays.asList("/mypage", "/reservations", "/payment", "/payments"),
                Arrays.asList("/oauth", "/auth"),
                Arrays.asList("/loginProc")
        );
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String method = request.getMethod();
        if (!StringUtils.hasText(method)) return true;

        String upperMethod = method.toUpperCase();
        if ("OPTIONS".equals(upperMethod)) return true;
        if (!STATE_CHANGING_METHODS.contains(upperMethod)) return true;

        String path = resolvePath(request);
        if (!isIncludedPath(path)) return true;
        return isExcludedPath(path);
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String origin = trimToNull(request.getHeader("Origin"));
        String referer = trimToNull(request.getHeader("Referer"));

        boolean allowed;
        if (StringUtils.hasText(origin)) {
            allowed = isAllowedOrigin(origin);
        } else {
            allowed = isAllowedReferer(referer);
        }

        if (!allowed) {
            log.warn("[OriginRefererCheck] blocked. method={}, requestURI={}, origin={}, referer={}",
                    request.getMethod(), request.getRequestURI(), origin, referer);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("Forbidden");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isAllowedOrigin(String origin) {
        if (!StringUtils.hasText(origin)) return false;
        return allowedOrigins.contains(normalizeOrigin(origin));
    }

    private boolean isAllowedReferer(String referer) {
        if (!StringUtils.hasText(referer)) return false;
        for (String allowedOrigin : allowedOrigins) {
            if (referer.equals(allowedOrigin) || referer.startsWith(allowedOrigin + "/")) {
                return true;
            }
        }
        return false;
    }

    private boolean isIncludedPath(String path) {
        for (String basePath : includeBasePaths) {
            if (matchesBasePath(path, basePath)) return true;
        }
        return false;
    }

    private boolean isExcludedPath(String path) {
        if (excludeExactPaths.contains(path)) return true;
        for (String basePath : excludeBasePaths) {
            if (matchesBasePath(path, basePath)) return true;
        }
        return false;
    }

    private boolean matchesBasePath(String path, String basePath) {
        return path.equals(basePath) || path.startsWith(basePath + "/");
    }

    private String resolvePath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (StringUtils.hasText(contextPath) && uri.startsWith(contextPath)) {
            return uri.substring(contextPath.length());
        }
        return uri;
    }

    private Set<String> normalizeOrigins(Collection<String> origins) {
        Set<String> normalized = new LinkedHashSet<>();
        if (origins == null) return normalized;
        for (String origin : origins) {
            if (StringUtils.hasText(origin)) {
                normalized.add(normalizeOrigin(origin));
            }
        }
        return normalized;
    }

    private List<String> normalizePaths(Collection<String> paths) {
        List<String> normalized = new ArrayList<>();
        if (paths == null) return normalized;
        for (String p : paths) {
            if (StringUtils.hasText(p)) {
                normalized.add(normalizePath(p));
            }
        }
        return normalized;
    }

    private String normalizeOrigin(String origin) {
        String value = origin.trim();
        while (value.length() > 1 && value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value.toLowerCase(Locale.ROOT);
    }

    private String normalizePath(String path) {
        String value = path.trim();
        if (!value.startsWith("/")) {
            value = "/" + value;
        }
        while (value.length() > 1 && value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) return null;
        return value.trim();
    }
}
