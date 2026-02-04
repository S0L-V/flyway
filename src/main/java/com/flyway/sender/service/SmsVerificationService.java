package com.flyway.sender.service;

import com.flyway.sender.domain.SmsVerification;
import com.flyway.sender.mapper.SmsVerificationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsVerificationService {

    private final SmsVerificationMapper mapper;
    private final SmsService smsService;

    @Transactional
    public void sendCode(String phoneNumber) {
        // 5분 내 3회 제한
        int count = mapper.countRecentByPhone(phoneNumber, LocalDateTime.now().minusMinutes(5));
        if (count >= 3) {
            throw new RuntimeException("잠시 후 다시 시도해주세요.");
        }

        String code = String.format("%06d", new Random().nextInt(1000000));

        SmsVerification v = SmsVerification.builder()
                .smsVerificationId(UUID.randomUUID().toString())
                .phoneNumber(phoneNumber)
                .code(code)
                .expiresAt(LocalDateTime.now().plusMinutes(3))
                .build();
        mapper.insert(v);

        smsService.sendSms(phoneNumber, "[Flyway] 인증번호 [" + code + "]");
        log.info("[SMS] 인증코드 발송 - {}", phoneNumber.substring(0, 3) + "****");
    }

    @Transactional
    public boolean verify(String phoneNumber, String code) {
        SmsVerification v = mapper.findLatestByPhone(phoneNumber);
        if (v == null) return false;
        if (v.getExpiresAt().isBefore(LocalDateTime.now())) return false;
        if (!v.getCode().equals(code)) return false;

        mapper.markVerified(v.getSmsVerificationId(), LocalDateTime.now());
        return true;
    }

    // 회원가입 시 인증 완료 여부 확인 (10분 내 인증 완료된 번호인지)
    public boolean isVerified(String phoneNumber) {
        return mapper.existsVerifiedPhone(phoneNumber, LocalDateTime.now().minusMinutes(10)) > 0;
    }
}