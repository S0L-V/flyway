package com.flyway.security.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class WebLoginRedirectEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        String path = resolvePath(request);

        /* 무한 루프 방지 */
        if ("/login".equals(path) || "/login/".equals(path)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String returnUrl = buildReturnUrl(request);
        String encoded = URLEncoder.encode(returnUrl, StandardCharsets.UTF_8);
        response.sendRedirect(request.getContextPath() + "/login?returnUrl=" + encoded);
    }

    private String resolvePath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String ctx = request.getContextPath();
        return (ctx != null && !ctx.isEmpty() && uri.startsWith(ctx)) ? uri.substring(ctx.length()) : uri;
    }

    private String buildReturnUrl(HttpServletRequest request) {
        String method = request.getMethod();
        if (method != null && !method.equalsIgnoreCase("GET")) {
            return "/";
        }
        String path = resolvePath(request);
        String query = request.getQueryString();
        String raw = (query != null && !query.isBlank()) ? path + "?" + query : path;
        if (raw == null || raw.isBlank()) return "/";
        if (!raw.startsWith("/")) return "/";
        if (raw.startsWith("//") || raw.startsWith("/\\")) return "/";
        String lower = raw.toLowerCase();
        if (lower.startsWith("/http") || raw.contains("://")) return "/";
        return raw;
    }
}
