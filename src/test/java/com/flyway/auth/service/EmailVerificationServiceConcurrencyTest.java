package com.flyway.auth.service;

import com.flyway.auth.domain.EmailVerificationPurpose;
import com.flyway.auth.domain.EmailVerificationToken;
import com.flyway.auth.repository.EmailVerificationRepository;
import com.flyway.auth.util.TokenHasher;
import com.flyway.template.common.mail.MailSender;
import com.flyway.user.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class EmailVerificationServiceConcurrencyTest {

    private EmailVerificationServiceImpl service;
    private InMemoryEmailVerificationRepository emailVerificationRepository;

    @BeforeEach
    void setUp() {
        emailVerificationRepository = new InMemoryEmailVerificationRepository();
        MailSender mailSender = Mockito.mock(MailSender.class);
        TokenHasher tokenHasher = Mockito.mock(TokenHasher.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);

        service = new EmailVerificationServiceImpl(
                emailVerificationRepository,
                mailSender,
                tokenHasher,
                userMapper
        );
        ReflectionTestUtils.setField(service, "baseUrl", "http://localhost:8080");
        ReflectionTestUtils.setField(service, "ttlMinutes", 15L);

        when(userMapper.findByEmailForLogin(anyString())).thenReturn(null);
        when(tokenHasher.hash(anyString()))
                .thenAnswer(invocation -> invocation.getArgument(0, String.class));
    }

    @Test
    @DisplayName("동시에 여러 번 발급해도 각 요청이 정상 처리된다")
    void issueSignupVerification_concurrent() throws Exception {
        String email = "multi@example.com";
        int threads = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);

        try {
            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                futures.add(executor.submit(() -> {
                    ready.countDown();
                    start.await();
                    service.issueSignupVerification(email);
                    return null;
                }));
            }

            assertTrue(ready.await(3, TimeUnit.SECONDS));
            start.countDown();

            for (Future<?> future : futures) {
                future.get(5, TimeUnit.SECONDS);
            }

            assertThat(emailVerificationRepository.countByEmail(email)).isEqualTo(threads);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    @DisplayName("동일 토큰 동시 검증은 성공 또는 실패로 일관되게 처리된다")
    void verifySignupToken_concurrent() throws Exception {
        String email = "verify@example.com";
        String token = "token-123";
        String tokenHash = token;
        LocalDateTime now = LocalDateTime.now();

        EmailVerificationToken record = EmailVerificationToken.builder()
                .emailVerificationTokenId(UUID.randomUUID().toString())
                .email(email)
                .purpose(EmailVerificationPurpose.SIGNUP)
                .tokenHash(tokenHash)
                .expiresAt(now.plusMinutes(10))
                .createdAt(now)
                .build();
        emailVerificationRepository.insertEmailVerificationToken(record);

        int threads = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        try {
            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                futures.add(executor.submit(() -> {
                    ready.countDown();
                    start.await();
                    try {
                        service.verifySignupToken(token);
                        successCount.incrementAndGet();
                    } catch (IllegalArgumentException e) {
                        failCount.incrementAndGet();
                    }
                    return null;
                }));
            }

            assertTrue(ready.await(3, TimeUnit.SECONDS));
            start.countDown();

            for (Future<?> future : futures) {
                future.get(5, TimeUnit.SECONDS);
            }

            assertThat(successCount.get()).isGreaterThanOrEqualTo(1);
            assertThat(successCount.get() + failCount.get()).isEqualTo(threads);

            assertThat(emailVerificationRepository.countUsed()).isEqualTo(1);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    @DisplayName("재발급과 검증이 동시에 실행되어도 정상 처리된다")
    void issueAndVerify_concurrent() throws Exception {
        String email = "race@example.com";
        String token = "race-token";
        String tokenHash = token;
        LocalDateTime now = LocalDateTime.now();

        EmailVerificationToken record = EmailVerificationToken.builder()
                .emailVerificationTokenId(UUID.randomUUID().toString())
                .email(email)
                .purpose(EmailVerificationPurpose.SIGNUP)
                .tokenHash(tokenHash)
                .expiresAt(now.plusMinutes(10))
                .createdAt(now)
                .build();
        emailVerificationRepository.insertEmailVerificationToken(record);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        try {
            Future<?> verifyFuture = executor.submit(() -> {
                ready.countDown();
                start.await();
                service.verifySignupToken(token);
                return null;
            });

            Future<?> issueFuture = executor.submit(() -> {
                ready.countDown();
                start.await();
                service.issueSignupVerification(email);
                return null;
            });

            assertTrue(ready.await(3, TimeUnit.SECONDS));
            start.countDown();

            assertThatCode(() -> verifyFuture.get(5, TimeUnit.SECONDS)).doesNotThrowAnyException();
            assertThatCode(() -> issueFuture.get(5, TimeUnit.SECONDS)).doesNotThrowAnyException();

            assertThat(emailVerificationRepository.countByEmail(email)).isGreaterThanOrEqualTo(2);
        } finally {
            executor.shutdownNow();
        }
    }

    private static final class InMemoryEmailVerificationRepository implements EmailVerificationRepository {
        private final ConcurrentHashMap<String, EmailVerificationToken> byHash = new ConcurrentHashMap<>();
        private final ConcurrentHashMap<String, EmailVerificationToken> byId = new ConcurrentHashMap<>();

        @Override
        public void insertEmailVerificationToken(EmailVerificationToken token) {
            if (byHash.putIfAbsent(token.getTokenHash(), token) != null) {
                throw new DuplicateKeyException("token_hash");
            }
            byId.put(token.getEmailVerificationTokenId(), token);
        }

        @Override
        public EmailVerificationToken findByTokenHash(String tokenHash) {
            return byHash.get(tokenHash);
        }

        @Override
        public int markTokenUsed(String emailVerificationTokenId, LocalDateTime usedAt) {
            synchronized (this) {
                EmailVerificationToken current = byId.get(emailVerificationTokenId);
                if (current == null || current.getUsedAt() != null) {
                    return 0;
                }
                EmailVerificationToken updated = EmailVerificationToken.builder()
                        .emailVerificationTokenId(current.getEmailVerificationTokenId())
                        .email(current.getEmail())
                        .purpose(current.getPurpose())
                        .tokenHash(current.getTokenHash())
                        .expiresAt(current.getExpiresAt())
                        .usedAt(usedAt)
                        .createdAt(current.getCreatedAt())
                        .build();
                byId.put(emailVerificationTokenId, updated);
                byHash.put(updated.getTokenHash(), updated);
                return 1;
            }
        }

        @Override
        public int countVerifiedByEmailPurpose(String email, String purpose, LocalDateTime now) {
            int count = 0;
            for (EmailVerificationToken token : byId.values()) {
                if (!email.equals(token.getEmail())) {
                    continue;
                }
                if (token.getPurpose() == null || !purpose.equals(token.getPurpose().name())) {
                    continue;
                }
                if (token.getUsedAt() == null) {
                    continue;
                }
                if (token.getExpiresAt() != null && token.getExpiresAt().isBefore(now)) {
                    continue;
                }
                count++;
            }
            return count;
        }

        int countByEmail(String email) {
            int count = 0;
            for (EmailVerificationToken token : byId.values()) {
                if (email.equals(token.getEmail())) {
                    count++;
                }
            }
            return count;
        }

        int countUsed() {
            int count = 0;
            for (EmailVerificationToken token : byId.values()) {
                if (token.getUsedAt() != null) {
                    count++;
                }
            }
            return count;
        }
    }
}
