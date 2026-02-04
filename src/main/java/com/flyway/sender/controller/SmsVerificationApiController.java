package com.flyway.sender.controller;

import com.flyway.sender.service.SmsVerificationService;
import com.flyway.template.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sms")
@RequiredArgsConstructor
public class SmsVerificationApiController {

    private final SmsVerificationService service;

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<Void>> send(@RequestParam String phoneNumber) {
        try {
            service.sendCode(phoneNumber);
            return ResponseEntity.ok(ApiResponse.success(null, "인증번호가 발송되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("FAIL", e.getMessage()));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<Boolean>> verify(
            @RequestParam String phoneNumber,
            @RequestParam String code) {
        boolean ok = service.verify(phoneNumber, code);
        String msg = ok ? "인증되었습니다." : "인증번호가 일치하지 않습니다.";
        return ResponseEntity.ok(ApiResponse.success(ok, msg));
    }
}