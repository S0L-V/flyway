package com.flyway.auth.service;

import com.flyway.auth.domain.EmailVerificationPurpose;
import com.flyway.auth.domain.EmailVerificationToken;
import com.flyway.auth.repository.EmailVerificationTokenMapper;
import com.flyway.auth.util.TokenHasher;
import com.flyway.template.common.mail.MailSender;
import com.flyway.user.mapper.UserMapper;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = EmailVerificationServiceConcurrencyTest.TestConfig.class)
class EmailVerificationServiceConcurrencyTest {

    @Autowired
    private EmailVerificationService emailVerificationService;

    @Autowired
    private EmailVerificationTokenMapper emailVerificationTokenMapper;

    @Autowired
    private TokenHasher tokenHasher;

    @Autowired
    private DataSource dataSource;

    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute("DROP TABLE IF EXISTS email_verification_token");
        jdbcTemplate.execute(
                "CREATE TABLE email_verification_token (" +
                        "email_verification_token_id CHAR(36) PRIMARY KEY," +
                        "email VARCHAR(255) NOT NULL," +
                        "purpose VARCHAR(32) NOT NULL," +
                        "token_hash VARCHAR(255) NOT NULL UNIQUE," +
                        "expires_at TIMESTAMP NOT NULL," +
                        "used_at TIMESTAMP NULL," +
                        "created_at TIMESTAMP NOT NULL" +
                        ")"
        );
    }

    @Test
    @DisplayName("동시에 여러 번 발급해도 각 요청이 정상 처리된다")
    void issueSignupVerification_concurrent() throws Exception {
        String email = "multi@example.com";
        int threads = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);

        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            futures.add(executor.submit(() -> {
                ready.countDown();
                start.await();
                emailVerificationService.issueSignupVerification(email);
                return null;
            }));
        }

        ready.await(3, TimeUnit.SECONDS);
        start.countDown();

        for (Future<?> future : futures) {
            future.get(5, TimeUnit.SECONDS);
        }

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM email_verification_token WHERE email = ?",
                Integer.class,
                email
        );
        assertThat(count).isEqualTo(threads);
        executor.shutdownNow();
    }

    @Test
    @DisplayName("동일 토큰 동시 검증은 성공 또는 실패로 일관되게 처리된다")
    void verifySignupToken_concurrent() throws Exception {
        String email = "verify@example.com";
        String token = "token-123";
        String tokenHash = tokenHasher.hash(token);
        LocalDateTime now = LocalDateTime.now();

        EmailVerificationToken record = EmailVerificationToken.builder()
                .emailVerificationTokenId(UUID.randomUUID().toString())
                .email(email)
                .purpose(EmailVerificationPurpose.SIGNUP)
                .tokenHash(tokenHash)
                .expiresAt(now.plusMinutes(10))
                .createdAt(now)
                .build();
        emailVerificationTokenMapper.insertEmailVerificationToken(record);

        int threads = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            futures.add(executor.submit(() -> {
                ready.countDown();
                start.await();
                try {
                    emailVerificationService.verifySignupToken(token);
                    successCount.incrementAndGet();
                } catch (IllegalArgumentException e) {
                    failCount.incrementAndGet();
                }
                return null;
            }));
        }

        ready.await(3, TimeUnit.SECONDS);
        start.countDown();

        for (Future<?> future : futures) {
            future.get(5, TimeUnit.SECONDS);
        }

        assertThat(successCount.get()).isGreaterThanOrEqualTo(1);
        assertThat(successCount.get() + failCount.get()).isEqualTo(threads);

        Integer usedCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM email_verification_token WHERE used_at IS NOT NULL",
                Integer.class
        );
        assertThat(usedCount).isEqualTo(1);
        executor.shutdownNow();
    }

    @Test
    @DisplayName("재발급과 검증이 동시에 실행되어도 정상 처리된다")
    void issueAndVerify_concurrent() throws Exception {
        String email = "race@example.com";
        String token = "race-token";
        String tokenHash = tokenHasher.hash(token);
        LocalDateTime now = LocalDateTime.now();

        EmailVerificationToken record = EmailVerificationToken.builder()
                .emailVerificationTokenId(UUID.randomUUID().toString())
                .email(email)
                .purpose(EmailVerificationPurpose.SIGNUP)
                .tokenHash(tokenHash)
                .expiresAt(now.plusMinutes(10))
                .createdAt(now)
                .build();
        emailVerificationTokenMapper.insertEmailVerificationToken(record);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        Future<?> verifyFuture = executor.submit(() -> {
            ready.countDown();
            start.await();
            emailVerificationService.verifySignupToken(token);
            return null;
        });

        Future<?> issueFuture = executor.submit(() -> {
            ready.countDown();
            start.await();
            emailVerificationService.issueSignupVerification(email);
            return null;
        });

        ready.await(3, TimeUnit.SECONDS);
        start.countDown();

        assertThatCode(() -> verifyFuture.get(5, TimeUnit.SECONDS)).doesNotThrowAnyException();
        assertThatCode(() -> issueFuture.get(5, TimeUnit.SECONDS)).doesNotThrowAnyException();

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM email_verification_token WHERE email = ?",
                Integer.class,
                email
        );
        assertThat(count).isGreaterThanOrEqualTo(2);
        executor.shutdownNow();
    }

    @Configuration
    @MapperScan("com.flyway.auth.repository")
    @Import({EmailVerificationServiceImpl.class, TokenHasher.class})
    static class TestConfig {

        @Bean
        public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
            Properties props = new Properties();
            props.setProperty("app.base-url", "http://localhost:8080");
            props.setProperty("mail.token.pepper", "test-pepper");
            props.setProperty("mail.verify.ttl-minutes", "1");
            PropertySourcesPlaceholderConfigurer config = new PropertySourcesPlaceholderConfigurer();
            config.setProperties(props);
            return config;
        }

        @Bean
        public DataSource dataSource() {
            return new EmbeddedDatabaseBuilder()
                    .setType(EmbeddedDatabaseType.H2)
                    .build();
        }

        @Bean
        public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
            SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
            factoryBean.setDataSource(dataSource);
            factoryBean.setMapperLocations(
                    new PathMatchingResourcePatternResolver()
                            .getResources("classpath:mapper/EmailVerificationTokenMapper.xml")
            );
            factoryBean.setTypeAliasesPackage("com.flyway.auth.domain");
            org.apache.ibatis.session.Configuration configuration =
                    new org.apache.ibatis.session.Configuration();
            configuration.setMapUnderscoreToCamelCase(true);
            factoryBean.setConfiguration(configuration);
            return factoryBean.getObject();
        }

        @Bean
        public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
            return new SqlSessionTemplate(sqlSessionFactory);
        }

        @Bean
        public MailSender mailSender() {
            return new MailSender() {
                @Override
                public void sendText(String to, String subject, String text) {
                }

                @Override
                public void sendHtml(String to, String subject, String html) {
                }
            };
        }

        @Bean
        public UserMapper userMapper() {
            return new UserMapper() {
                @Override
                public void insertUser(com.flyway.user.domain.User user) {
                    throw new UnsupportedOperationException("test stub");
                }

                @Override
                public com.flyway.user.domain.User findById(String userId) {
                    return null;
                }

                @Override
                public com.flyway.user.domain.User findByEmailForLogin(String email) {
                    return null;
                }

                @Override
                public void updateEmail(String userId, String email) {
                    throw new UnsupportedOperationException("test stub");
                }

                @Override
                public void updateStatus(String userId, String status) {
                    throw new UnsupportedOperationException("test stub");
                }
            };
        }
    }
}
