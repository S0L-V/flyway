package com.flyway.security.handler;

import com.flyway.auth.service.AuthTokenService;
import com.flyway.security.principal.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    public static final String REDIRECT_PATH_ATTRIBUTE = "AUTH_REDIRECT_PATH";

    private final AuthTokenService authTokenService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        String userId = extractUserId(authentication);
        authTokenService.issueLoginCookies(request, response, userId);

        log.debug("[AUTH] login success. userId={}", userId);
        redirectToTarget(request, response);
    }

    private String extractUserId(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getUserId();
        }

        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }

        throw new IllegalStateException("Unsupported principal type: " + principal.getClass());
    }

    private void redirectToTarget(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String targetUrl = request.getContextPath() + resolveTargetPath(request);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private String resolveTargetPath(HttpServletRequest request) {
        Object attribute = request.getAttribute(REDIRECT_PATH_ATTRIBUTE);
        if (attribute instanceof String) {
            String path = ((String) attribute).trim();
            if (path.startsWith("/")) {
                return path;
            }
        }
        return "/";
    }
}
