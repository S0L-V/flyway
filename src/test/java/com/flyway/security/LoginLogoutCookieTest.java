package com.flyway.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flyway.security.config.SecurityConfigWeb;
import com.flyway.security.handler.JwtAuthenticationEntryPoint;
import com.flyway.security.handler.LoginSuccessHandler;
import com.flyway.security.jwt.JwtProperties;
import com.flyway.security.jwt.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import javax.servlet.http.Cookie;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {
        LoginLogoutCookieTest.TestSecurityBeans.class,
        SecurityConfigWeb.class
})
@TestPropertySource(properties = {
        "jwt.user.secret=0123456789abcdef0123456789abcdef",
        "jwt.user.accessTokenTtlSeconds=3600",
        "jwt.user.refreshTokenTtlSeconds=7200",
        "jwt.user.issuer=flyway-test"
})
class LoginLogoutCookieTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private javax.servlet.Filter springSecurityFilterChain;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(springSecurityFilterChain)
                .build();
    }

    @Test
    @DisplayName("로그인 시 accessToken과 JSESSIONID 쿠키가 내려간다")
    void login_adds_accessToken_and_jsessionid_cookies() throws Exception {
        when(jwtProvider.createAccessToken("user-123")).thenReturn("access.jwt.token");

        MvcResult result = mockMvc.perform(post("/loginProc")
                        .param("username", "user-123")
                        .param("password", "pw"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        assertTrue(hasCookie(result, "accessToken"));
        assertTrue(hasCookie(result, "JSESSIONID") || hasSessionId(result));
    }

    @Test
    @DisplayName("로그아웃 시 accessToken과 JSESSIONID 쿠키가 삭제된다")
    void logout_clears_accessToken_and_jsessionid_cookies() throws Exception {
        when(jwtProvider.createAccessToken("user-123")).thenReturn("access.jwt.token");

        MvcResult loginResult = mockMvc.perform(post("/loginProc")
                        .param("username", "user-123")
                        .param("password", "pw"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        assertNotNull(session);

        MvcResult logoutResult = mockMvc.perform(post("/logout").session(session))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        assertTrue(hasClearedCookie(logoutResult, "accessToken"));
        assertTrue(hasClearedCookie(logoutResult, "JSESSIONID") || session.isInvalid());
        assertTrue(session.isInvalid());
    }

    private boolean hasCookie(MvcResult result, String name) {
        Cookie cookie = result.getResponse().getCookie(name);
        if (cookie != null) {
            return true;
        }
        List<String> headers = result.getResponse().getHeaders(HttpHeaders.SET_COOKIE);
        return headers.stream().anyMatch(h -> h.startsWith(name + "="));
    }

    private boolean hasClearedCookie(MvcResult result, String name) {
        Cookie cookie = result.getResponse().getCookie(name);
        if (cookie != null) {
            return cookie.getMaxAge() == 0;
        }
        List<String> headers = result.getResponse().getHeaders(HttpHeaders.SET_COOKIE);
        return headers.stream().anyMatch(h -> h.startsWith(name + "=") && h.contains("Max-Age=0"));
    }

    private boolean hasSessionId(MvcResult result) {
        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        return session != null && session.getId() != null && !session.getId().isBlank();
    }

    @Configuration
    @EnableWebSecurity
    static class TestSecurityBeans {

        @Bean
        static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
            return new PropertySourcesPlaceholderConfigurer();
        }

        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }

        @Bean
        JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
            return new JwtAuthenticationEntryPoint(objectMapper);
        }

        @Bean
        PasswordEncoder passwordEncoder() {
            return new PasswordEncoder() {
                @Override
                public String encode(CharSequence rawPassword) {
                    return rawPassword == null ? null : rawPassword.toString();
                }

                @Override
                public boolean matches(CharSequence rawPassword, String encodedPassword) {
                    if (rawPassword == null || encodedPassword == null) {
                        return false;
                    }
                    return rawPassword.toString().equals(encodedPassword);
                }
            };
        }

        @Bean(name = "emailUserDetailsService")
        UserDetailsService emailUserDetailsService() {
            return username -> User.withUsername(username)
                    .password("pw")
                    .authorities("ROLE_USER")
                    .build();
        }

        @Bean(name = "userIdUserDetailsService")
        UserDetailsService userIdUserDetailsService() {
            return username -> User.withUsername(username)
                    .password("pw")
                    .authorities("ROLE_USER")
                    .build();
        }

        @Bean
        JwtProvider jwtProvider() {
            return Mockito.mock(JwtProvider.class);
        }

        @Bean
        JwtProperties jwtProperties() {
            JwtProperties props = new JwtProperties();
            props.setSecret("0123456789abcdef0123456789abcdef");
            props.setAccessTokenTtlSeconds(3600);
            props.setRefreshTokenTtlSeconds(7200);
            props.setIssuer("flyway-test");
            return props;
        }

        @Bean
        LoginSuccessHandler loginSuccessHandler(JwtProvider jwtProvider, JwtProperties jwtProperties) {
            return new LoginSuccessHandler(jwtProvider, jwtProperties);
        }
    }
}
