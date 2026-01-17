package com.flyway.auth.controller;

import com.flyway.auth.dto.EmailSignUpRequest;
import com.flyway.auth.service.KakaoLoginService;
import com.flyway.auth.service.SignUpService;
import com.flyway.security.principal.CustomUserDetails;
import com.flyway.template.exception.BusinessException;
import com.flyway.template.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthController {

    private final SignUpService signUpService;
    private final KakaoLoginService kakaoLoginService;

    @PostMapping("/auth/signup")
    public String postSignUp(
            @ModelAttribute("form") EmailSignUpRequest form,
            @RequestParam(value = "oauthSignUp", required = false) Boolean oauthSignUp,
            Model model
    ) {
        try {
            if (Boolean.TRUE.equals(oauthSignUp)) {
                String userId = extractAuthenticatedUserId();
                signUpService.completeOauthSignUp(userId, form);
                return "redirect:/";
            } else {
                signUpService.signUp(form);
                return "redirect:/login";
            }
        } catch (BusinessException | IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            return "signup";
        } catch (Exception e) {
            log.error(e.getMessage());
            model.addAttribute("error", "회원가입 처리 중 오류가 발생했습니다.");
            return "signup";
        }
    }

    @GetMapping("/auth/kakao")
    public void kakaoLogin(HttpServletRequest req, HttpServletResponse res) throws IOException {
        kakaoLoginService.redirectToKakao(req, res);
    }

    @GetMapping("/auth/kakao/callback")
    public void kakaoCallback(
            @RequestParam String code,
            @RequestParam(required = false) String state,
            HttpServletRequest req,
            HttpServletResponse res
    ) throws IOException {
        kakaoLoginService.handleCallback(code, state, req, res);
    }

    private String extractAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getUserId();
        }
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            return ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
        }
        throw new BusinessException(ErrorCode.UNAUTHORIZED);
    }
}
