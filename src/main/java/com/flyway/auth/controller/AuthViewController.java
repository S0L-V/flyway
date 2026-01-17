package com.flyway.auth.controller;

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
    public String signupView(HttpSession session, Model model) {
        Object oauthFlag = session.getAttribute(OAUTH_SIGNUP_FLAG_ATTR);
        Object oauthEmail = session.getAttribute(OAUTH_SIGNUP_EMAIL_ATTR);

        if (Boolean.TRUE.equals(oauthFlag)) {
            model.addAttribute("oauthSignUp", true);
            if (oauthEmail instanceof String) {
                model.addAttribute("signupEmail", oauthEmail);
            }
        }

        session.removeAttribute(OAUTH_SIGNUP_FLAG_ATTR);
        session.removeAttribute(OAUTH_SIGNUP_EMAIL_ATTR);

        return "signup";
    }

}
