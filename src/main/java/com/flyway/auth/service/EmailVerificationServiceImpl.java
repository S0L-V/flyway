package com.flyway.auth.service;

import com.flyway.auth.domain.EmailVerificationPurpose;
import com.flyway.auth.domain.EmailVerificationToken;
import com.flyway.auth.domain.SignUpAttempt;
import com.flyway.auth.domain.SignUpStatus;
import com.flyway.auth.repository.EmailVerificationRepository;
import com.flyway.auth.repository.SignUpAttemptRepository;
import com.flyway.auth.util.TokenHasher;
import com.flyway.template.common.mail.MailSender;
import com.flyway.template.exception.BusinessException;
import com.flyway.template.exception.ErrorCode;
import com.flyway.template.exception.MailSendException;
import com.flyway.template.util.MaskUtil;
import com.flyway.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
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
    private static final String INVALID_LINK_MESSAGE = "유효하지 않은 인증 링크입니다.";
    private static final String USED_LINK_MESSAGE = "이미 사용된 인증 링크입니다.";
    private static final String EXPIRED_LINK_MESSAGE = "만료된 인증 링크입니다.";
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final EmailVerificationRepository emailVerificationRepository;
    private final SignUpAttemptRepository signUpAttemptRepository;
    private final MailSender mailSender;
    private final TokenHasher tokenHasher;
    private final UserMapper userMapper;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${mail.verify.ttl-minutes}")
    private long ttlMinutes;

    @Value("${signup.attempt.ttl-minutes}")
    private long attemptTtlMinutes;

    @Transactional
    @Override
    public String issueSignupVerification(String email) {
        validateEmail(email);
        ensureEmailNotRegistered(email);

        LocalDateTime now = LocalDateTime.now();

        SignUpAttempt attempt = buildSignUpAttempt(email, now);
        signUpAttemptRepository.insert(attempt);

        String attemptId = attempt.getAttemptId();
        String token = insertTokenWithRetry(email, attemptId, now);
        sendVerificationEmailAfterCommit(email, token, attemptId);
        return attemptId;
    }

    @Transactional
    @Override
    public String verifySignupToken(String token, String attemptId) {
        if (!StringUtils.hasText(token) || !StringUtils.hasText(attemptId)) {
            throw new IllegalArgumentException(INVALID_LINK_MESSAGE);
        }

        String tokenHash = tokenHasher.hash(token);
        EmailVerificationToken stored = emailVerificationRepository.findByTokenHash(tokenHash);
        if (stored == null) {
            throw new IllegalArgumentException(INVALID_LINK_MESSAGE);
        }
        if (stored.getUsedAt() != null) {
            throw new IllegalArgumentException(USED_LINK_MESSAGE);
        }
        if (!attemptId.equals(stored.getAttemptId())) {
            throw new IllegalArgumentException(INVALID_LINK_MESSAGE);
        }

        LocalDateTime now = LocalDateTime.now();
        if (stored.getExpiresAt() != null && stored.getExpiresAt().isBefore(now)) {
            throw new IllegalArgumentException(EXPIRED_LINK_MESSAGE);
        }

        int tokenUpdated = emailVerificationRepository.markTokenUsed(stored.getEmailVerificationTokenId(), now);
        if (tokenUpdated == 0) {
            throw new IllegalArgumentException(USED_LINK_MESSAGE);
        }

        int attemptUpdated = signUpAttemptRepository.markVerifiedIfPending(attemptId, now);
        if (attemptUpdated == 0) {
            throw new IllegalArgumentException("이미 처리된 인증 요청입니다.");
        }

        return stored.getEmail();
    }

    @Override
    public boolean isSignupVerified(String email, String attemptId) {
        validateEmail(email);
        int count = emailVerificationRepository.existsUsedTokenByEmailAttempt(
                email,
                attemptId,
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

    private void sendVerificationEmailAfterCommit(String email, String token, String attemptId) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            sendVerificationEmail(email, token, attemptId);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                sendVerificationEmail(email, token, attemptId);
            }
        });
    }

    private void sendVerificationEmail(String email, String token, String attemptId) {
        String verifyUrl = buildVerifyUrl(token, attemptId);
        String body = ""
                + "Flyway 이메일 인증 안내입니다.\n\n"
                + "아래 링크를 클릭하여 인증을 완료해 주세요.\n"
                + verifyUrl + "\n\n"
                + "본 메일은 인증 요청 시에만 발송됩니다.\n";

        try {
            mailSender.sendText(email, SUBJECT, body);
        } catch (Exception e) {
            log.error("[AUTH] failed to send verification email. email={}", MaskUtil.maskEmail(email), e);
            throw new MailSendException("메일 전송 실패", e);
        }
    }

    private String buildVerifyUrl(String token, String attemptId) {
        String normalizedBaseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8);
        String encodedAttempt = URLEncoder.encode(attemptId, StandardCharsets.UTF_8);

        return normalizedBaseUrl + "/auth/email/verify?token=" + encodedToken + "&attempt=" + encodedAttempt;
    }

    private SignUpAttempt buildSignUpAttempt(String email, LocalDateTime now) {
        return SignUpAttempt.builder()
                .attemptId(UUID.randomUUID().toString())
                .email(email)
                .status(SignUpStatus.PENDING)
                .createdAt(now)
                .expiresAt(now.plusMinutes(attemptTtlMinutes))
                .build();
    }

    private String insertTokenWithRetry(String email, String attemptId, LocalDateTime now) {
        for (int retry = 1; retry <= MAX_RETRY; retry++) {
            String token = UUID.randomUUID().toString();
            String tokenHash = tokenHasher.hash(token);
            EmailVerificationToken record = EmailVerificationToken.builder()
                    .emailVerificationTokenId(UUID.randomUUID().toString())
                    .email(email)
                    .purpose(EmailVerificationPurpose.SIGNUP)
                    .tokenHash(tokenHash)
                    .expiresAt(now.plusMinutes(ttlMinutes))
                    .createdAt(now)
                    .attemptId(attemptId)
                    .build();

            try {
                emailVerificationRepository.insertEmailVerificationToken(record);
                return token;
            } catch (DuplicateKeyException e) {
                log.warn("[AUTH] duplicate token hash. retry={}", retry);
                if (retry == MAX_RETRY) {
                    throw e;
                }
            } catch (Exception e) {
                log.error("[AUTH] failed to save email verification token. email={}",
                        MaskUtil.maskEmail(email), e);
                throw e;
            }
        }

        throw new IllegalStateException("토큰 발급에 실패했습니다.");
    }
}
