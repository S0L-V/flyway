package com.flyway.security.filter;

import com.flyway.auth.domain.RefreshToken;
import com.flyway.auth.repository.RefreshTokenRepository;
import com.flyway.auth.service.AuthTokenService;
import com.flyway.auth.util.TokenHasher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class RefreshTokenSessionSyncFilter extends OncePerRequestFilter {

    private static final String REFRESH_COOKIE = "refreshToken";

    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenHasher tokenHasher;
    private final AuthTokenService authTokenService;
    private final List<String> excludePatterns;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String path = resolvePath(request);

        /* 필터 적용 제외 경로: 검증 없이 통과 */
        if (isExcluded(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!isAuthenticated(auth)) {
            filterChain.doFilter(request, response);
            return;
        }

        String refreshRaw = readCookie(request, REFRESH_COOKIE);
        if (!StringUtils.hasText(refreshRaw)) {
            forceLogoutAndRedirect(request, response, "missing_refresh");
            return;
        }

        String hash = tokenHasher.hash(refreshRaw);
        RefreshToken stored = refreshTokenRepository.findByTokenHash(hash);
        if (isInvalid(stored)) {
            forceLogoutAndRedirect(request, response, "invalid_refresh");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isInvalid(RefreshToken stored) {
        if (stored == null) return true;
        LocalDateTime now = LocalDateTime.now();
        return stored.getRevokedAt() != null
                || stored.getRotatedAt() != null
                || stored.getExpiresAt() == null
                || !stored.getExpiresAt().isAfter(now);
    }

    private void forceLogoutAndRedirect(
            HttpServletRequest request,
            HttpServletResponse response,
            String reason
    ) throws IOException {
        log.debug("[AUTH] force logout by refresh sync. reason={}, uri={}", reason, request.getRequestURI());
        authTokenService.forceLogout(request, response);
        response.sendRedirect(request.getContextPath() + "/login");
    }

    private boolean isAuthenticated(Authentication auth) {
        return auth != null
                && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken);
    }

    /* 요청 경로가 excludePatterns에 포함되는 경우 필터를 적용하지 않음 */
    private boolean isExcluded(String path) {
        for (String pattern : excludePatterns) {
            if (matches(path, pattern)) {
                return true;
            }
        }
        return false;
    }

    private boolean matches(String path, String pattern) {
        if (pattern == null || pattern.isEmpty()) return false;
        if (pattern.endsWith("/**")) {
            String prefix = pattern.substring(0, pattern.length() - 3);
            return path.startsWith(prefix);
        }
        return path.equals(pattern);
    }

    private String readCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie c : cookies) {
            if (name.equals(c.getName())) return c.getValue();
        }
        return null;
    }

    private String resolvePath(HttpServletRequest request) {
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && path.startsWith(contextPath)) {
            return path.substring(contextPath.length());
        }
        return path;
    }
}
