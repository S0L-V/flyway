package com.flyway.auth.controller;

import com.flyway.auth.dto.EmailSignUpRequest;
import com.flyway.auth.service.KakaoLoginService;
import com.flyway.auth.service.SignUpService;
import com.flyway.template.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
            Model model
    ) {
        try {
            signUpService.signUp(form);
            return "redirect:/login";
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
}
