package com.flyway.auth.controller;

import com.flyway.auth.service.EmailVerificationService;
import com.flyway.template.common.ApiResponse;
import com.flyway.template.exception.BusinessException;
import com.flyway.template.exception.ErrorCode;
import com.flyway.template.exception.MailSendException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class EmailVerificationApiController {

    private final EmailVerificationService emailVerificationService;

    @PostMapping("/api/auth/email/issue")
    public ResponseEntity<ApiResponse<Void>> issueSignupVerification(@RequestParam String email) {
        try {
            emailVerificationService.issueSignupVerification(email);
            return ResponseEntity.ok(ApiResponse.success(null, "이메일 인증 메일을 전송했습니다."));
        } catch (BusinessException e) {
            return ResponseEntity.status(e.getErrorCode().getStatus())
                    .body(ApiResponse.error(
                            e.getErrorCode().getCode(),
                            e.getMessage()
                    ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(ErrorCode.INVALID_INPUT_VALUE.getCode(), e.getMessage()));
        } catch (MailSendException e) {
            log.error("[AUTH] email verification send failed. email={}", email, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(
                            ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                            "메일 전송에 실패했습니다."
                    ));
        }
    }

    @GetMapping("/api/auth/email/status")
    public ResponseEntity<ApiResponse<Boolean>> checkSignupVerification(@RequestParam String email) {
        try {
            boolean verified = emailVerificationService.isSignupVerified(email);
            return ResponseEntity.ok(ApiResponse.success(verified));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(ErrorCode.INVALID_INPUT_VALUE.getCode(), e.getMessage()));
        }
    }

}
