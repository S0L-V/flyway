package com.flyway.auth.controller;

import com.flyway.auth.domain.AuthStatus;
import com.flyway.security.principal.CustomUserDetails;
import com.flyway.user.domain.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpSession;

@Controller
public class AuthViewController {

    private static final String OAUTH_SIGNUP_FLAG_ATTR = "OAUTH_SIGNUP";
    private static final String OAUTH_SIGNUP_EMAIL_ATTR = "OAUTH_SIGNUP_EMAIL";

    @GetMapping("/login")
    public String loginView() {
        return "login";
    }

    @GetMapping("/signup")
    public String signupView(
            HttpSession session,
            Model model,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        boolean oauthSignUp = Boolean.TRUE.equals(session.getAttribute(OAUTH_SIGNUP_FLAG_ATTR));
        String signupEmail = session.getAttribute(OAUTH_SIGNUP_EMAIL_ATTR) instanceof String
                ? (String) session.getAttribute(OAUTH_SIGNUP_EMAIL_ATTR)
                : null;

        if (!oauthSignUp && principal != null) {
            User user = principal.getUser();
            if (user != null
                    && AuthStatus.ONBOARDING.equals(user.getStatus())
                    && user.getPasswordHash() == null) {
                oauthSignUp = true;
                if (signupEmail == null || signupEmail.isBlank()) {
                    signupEmail = user.getEmail();
                }
            }
        }

        if (oauthSignUp) {
            model.addAttribute("oauthSignUp", true);
            if (signupEmail != null && !signupEmail.isBlank()) {
                model.addAttribute("signupEmail", signupEmail);
            }
        }

        session.removeAttribute(OAUTH_SIGNUP_FLAG_ATTR);
        session.removeAttribute(OAUTH_SIGNUP_EMAIL_ATTR);

        return "signup";
    }

}
