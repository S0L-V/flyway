package com.flyway.auth.controller;

import com.flyway.auth.service.EmailVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class EmailVerificationViewController {

    private final EmailVerificationService emailVerificationService;

    @GetMapping("/auth/email/verify")
    public String verifySignupToken(@RequestParam String token, Model model) {
        try {
            emailVerificationService.verifySignupToken(token);
            model.addAttribute("title", "이메일 인증 완료");
            model.addAttribute("statusMessage", "인증이 완료되었습니다.");
            model.addAttribute("hintMessage", "회원가입 페이지로 돌아가 인증 확인을 눌러 주세요.");
            model.addAttribute("success", true);
        } catch (IllegalArgumentException e) {
            model.addAttribute("title", "이메일 인증 실패");
            model.addAttribute("statusMessage", "유효하지 않은 인증 링크입니다.");
            model.addAttribute("hintMessage", "인증메일을 다시 요청해 주세요.");
            model.addAttribute("success", false);
        }
        return "email-verify";
    }
}
