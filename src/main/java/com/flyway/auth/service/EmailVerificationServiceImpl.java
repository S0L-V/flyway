package com.flyway.auth.service;

import com.flyway.auth.domain.EmailVerificationPurpose;
import com.flyway.auth.domain.EmailVerificationToken;
import com.flyway.auth.repository.EmailVerificationTokenMapper;
import com.flyway.auth.util.TokenHasher;
import com.flyway.template.common.mail.MailSender;
import com.flyway.template.exception.BusinessException;
import com.flyway.template.exception.ErrorCode;
import com.flyway.template.exception.MailSendException;
import com.flyway.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationServiceImpl implements EmailVerificationService {

    private static final String SUBJECT = "[Flyway] 이메일 인증 안내";
    private static final int MAX_RETRY = 3;
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final EmailVerificationTokenMapper emailVerificationTokenMapper;
    private final MailSender mailSender;
    private final TokenHasher tokenHasher;
    private final UserMapper userMapper;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${mail.verify.ttl-minutes:15}")
    private long ttlMinutes;

    @Override
    public void issueSignupVerification(String email) {
        validateEmail(email);
        ensureEmailNotRegistered(email);

        for (int attempt = 1; attempt <= MAX_RETRY; attempt++) {
            String token = UUID.randomUUID().toString();
            String tokenHash = tokenHasher.hash(token);
            LocalDateTime now = LocalDateTime.now();

            EmailVerificationToken record = EmailVerificationToken.builder()
                    .emailVerificationTokenId(UUID.randomUUID().toString())
                    .email(email)
                    .purpose(EmailVerificationPurpose.SIGNUP)
                    .tokenHash(tokenHash)
                    .expiresAt(now.plusMinutes(ttlMinutes))
                    .createdAt(now)
                    .build();

            try {
                emailVerificationTokenMapper.insertEmailVerificationToken(record);
            } catch (DuplicateKeyException e) {
                log.warn("[AUTH] duplicate token hash. retry={}", attempt);
                if (attempt == MAX_RETRY) {
                    throw e;
                }
                continue;
            } catch (Exception e) {
                log.error("[AUTH] failed to save email verification token. email={}", email, e);
                throw e;
            }

            sendVerificationEmail(email, token);
            return;
        }
    }

    @Override
    public String verifySignupToken(String token) {
        if (!StringUtils.hasText(token)) {
            throw new IllegalArgumentException("유효하지 않은 인증 링크입니다.");
        }

        String tokenHash = tokenHasher.hash(token);
        EmailVerificationToken stored = emailVerificationTokenMapper.findByTokenHash(tokenHash);
        if (stored == null) {
            throw new IllegalArgumentException("유효하지 않은 인증 링크입니다.");
        }
        if (stored.getUsedAt() != null) {
            throw new IllegalArgumentException("이미 사용된 인증 링크입니다.");
        }

        LocalDateTime now = LocalDateTime.now();
        if (stored.getExpiresAt() != null && stored.getExpiresAt().isBefore(now)) {
            throw new IllegalArgumentException("만료된 인증 링크입니다.");
        }

        emailVerificationTokenMapper.markTokenUsed(stored.getEmailVerificationTokenId(), now);
        return stored.getEmail();
    }

    @Override
    public boolean isSignupVerified(String email) {
        validateEmail(email);
        int count = emailVerificationTokenMapper.countVerifiedByEmailPurpose(
                email,
                EmailVerificationPurpose.SIGNUP.name(),
                LocalDateTime.now()
        );
        return count > 0;
    }

    private void validateEmail(String email) {
        if (!StringUtils.hasText(email) || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("이메일 형식이 올바르지 않습니다.");
        }
    }

    private void ensureEmailNotRegistered(String email) {
        if (userMapper.findByEmailForLogin(email) != null) {
            throw new BusinessException(ErrorCode.USER_EMAIL_ALREADY_EXISTS);
        }
    }

    private void sendVerificationEmail(String email, String token) {
        String verifyUrl = buildVerifyUrl(token);
        String body = ""
                + "Flyway 이메일 인증 안내입니다.\n\n"
                + "아래 링크를 클릭하여 인증을 완료해 주세요.\n"
                + verifyUrl + "\n\n"
                + "본 메일은 인증 요청 시에만 발송됩니다.\n";

        try {
            mailSender.sendText(email, SUBJECT, body);
        } catch (Exception e) {
            log.error("[AUTH] failed to send verification email. email={}", email, e);
            throw new MailSendException("메일 전송 실패", e);
        }
    }

    private String buildVerifyUrl(String token) {
        String normalizedBaseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8);
        return normalizedBaseUrl + "/auth/email/verify?token=" + encodedToken;
    }
}
