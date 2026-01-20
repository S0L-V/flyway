package com.flyway.auth.service;

import com.flyway.auth.domain.EmailVerificationPurpose;
import com.flyway.auth.domain.EmailVerificationToken;
import com.flyway.auth.repository.EmailVerificationTokenMapper;
import com.flyway.auth.util.TokenHasher;
import com.flyway.template.common.mail.MailSender;
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
import java.util.Properties;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = EmailVerificationServiceIntegrationTest.TestConfig.class)
class EmailVerificationServiceIntegrationTest {

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
    @DisplayName("만료된 토큰은 인증에 실패하고, 인증 상태는 false")
    void verifySignupToken_expiredToken_fails() {
        String email = "expired@example.com";
        String token = "expired-token";
        String tokenHash = tokenHasher.hash(token);
        LocalDateTime now = LocalDateTime.now();

        EmailVerificationToken record = EmailVerificationToken.builder()
                .emailVerificationTokenId(UUID.randomUUID().toString())
                .email(email)
                .purpose(EmailVerificationPurpose.SIGNUP)
                .tokenHash(tokenHash)
                .expiresAt(now.minusMinutes(1))
                .createdAt(now.minusMinutes(2))
                .build();

        emailVerificationTokenMapper.insertEmailVerificationToken(record);

        assertThatThrownBy(() -> emailVerificationService.verifySignupToken(token))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(emailVerificationService.isSignupVerified(email)).isFalse();
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
    }
}
