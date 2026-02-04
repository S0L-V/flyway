package com.flyway.sender.domain;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsVerification {

    private String smsVerificationId;
    private String phoneNumber;
    private String code;
    private LocalDateTime expiresAt;
    private LocalDateTime verifiedAt;
    private LocalDateTime createdAt;

}