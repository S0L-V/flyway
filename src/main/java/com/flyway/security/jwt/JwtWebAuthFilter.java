package com.flyway.security.jwt;

import com.flyway.security.handler.JwtAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationServiceException;
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
public class JwtWebAuthFilter extends OncePerRequestFilter {

    private static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
    private static final String ADMIN_PREFIX = "/admin/";

    private final JwtProvider jwtProvider;
    private final JwtAuthenticationEntryPoint entryPoint;
    private final UserDetailsService userIdUserDetailsService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return resolvePath(request).startsWith(ADMIN_PREFIX);
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            if (!isAuthenticated()) {
                String token = extractAccessTokenFromCookie(request);

                if (StringUtils.hasText(token)) {
                    String userId = jwtProvider.getSubjectOrThrow(token);

                    UserDetails userDetails = userIdUserDetailsService.loadUserByUsername(userId);

                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities()
                            );

                    SecurityContextHolder.getContext().setAuthentication(auth);

                    log.debug("[JWT][WEB] authenticated. uri={}, userId={}",
                            request.getRequestURI(), userId);
                }
            }

            filterChain.doFilter(request, response);

        } catch (BadCredentialsException e) {
            SecurityContextHolder.clearContext();
            log.warn("[JWT][WEB] bad credentials. uri={}, msg={}",
                    request.getRequestURI(), e.getMessage());
            entryPoint.commence(request, response, e);
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            log.error("[JWT][WEB] unexpected exception. uri={}", request.getRequestURI(), e);
            entryPoint.commence(
                    request,
                    response,
                    new AuthenticationServiceException("JWT authentication failed", e)
            );
        }
    }

    private boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null
                && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken);
    }

    private String extractAccessTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;

        for (Cookie c : cookies) {
            if (ACCESS_TOKEN_COOKIE_NAME.equals(c.getName())) {
                return c.getValue();
            }
        }
        return null;
    }

    private String resolvePath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String ctx = request.getContextPath();
        return (ctx != null && !ctx.isEmpty() && uri.startsWith(ctx)) ? uri.substring(ctx.length()) : uri;
    }
}
