package com.flyway.template.controller;

import com.flyway.template.common.mail.MailSender;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class MailTestController {

    private final MailSender mailSender;

    @Profile("dev")
    @GetMapping("/dev/mail")
    public String test(@RequestParam String to) {
        mailSender.sendText(to, "[Flyway] 메일 테스트", "메일 전송 테스트입니다.");
        return "redirect:/";
    }
}
