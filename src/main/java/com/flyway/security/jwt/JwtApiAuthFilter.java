package com.flyway.security.jwt;

import com.flyway.security.handler.JwtAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@Slf4j
@RequiredArgsConstructor
public class JwtApiAuthFilter extends OncePerRequestFilter {

    private static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
    private static final int TOKEN_LOG_PREFIX_LEN = 20;

    private final JwtProvider jwtProvider;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final UserDetailsService userIdUserDetailsService;

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = resolvePath(request);

        if (!path.startsWith("/api/")) {
            return true;
        }

        return path.startsWith("/api/public/")
                || path.startsWith("/api/auth/");
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest req,
            @NonNull HttpServletResponse res,
            @NonNull FilterChain chain
    ) throws ServletException, IOException {

        String uri = req.getRequestURI();
        String token = extractAccessTokenFromCookie(req);

        if (!StringUtils.hasText(token)) {
            log.debug("[JWT][API] no token. uri={}", uri);
            chain.doFilter(req, res);
            return;
        }

        if (isAlreadyAuthenticated()) {
            log.debug("[JWT][API] already authenticated. uri={}", uri);
            chain.doFilter(req, res);
            return;
        }

        try {
            authenticate(token);
            log.debug("[JWT][API] authenticated. uri={}, tokenPrefix={}", uri, safePrefix(token, TOKEN_LOG_PREFIX_LEN));
            chain.doFilter(req, res);

        } catch (BadCredentialsException e) {
            SecurityContextHolder.clearContext();
            log.warn("[JWT][API] bad credentials. uri={}, msg={}", uri, e.getMessage());
            authenticationEntryPoint.commence(req, res, e);

        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            log.error("[JWT][API] unexpected exception. uri={}", uri, e);
            authenticationEntryPoint.commence(req, res, new BadCredentialsException("JWT processing error", e));
        }
    }

    private void authenticate(String token) {
        String userId = jwtProvider.getSubjectOrThrow(token);

        UserDetails userDetails = userIdUserDetailsService.loadUserByUsername(userId);

        Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private String extractAccessTokenFromCookie(HttpServletRequest req) {
        Cookie[] cookies = req.getCookies();
        if (cookies == null) return null;

        for (Cookie cookie : cookies) {
            if (ACCESS_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private boolean isAlreadyAuthenticated() {
        Authentication existing = SecurityContextHolder.getContext().getAuthentication();
        return existing != null
                && existing.isAuthenticated()
                && !(existing instanceof AnonymousAuthenticationToken);
    }

    private String resolvePath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String ctx = request.getContextPath();
        return (ctx != null && !ctx.isEmpty() && uri.startsWith(ctx)) ? uri.substring(ctx.length()) : uri;
    }

    private String safePrefix(String token, int len) {
        int end = Math.min(len, token.length());
        return token.substring(0, end);
    }
}
