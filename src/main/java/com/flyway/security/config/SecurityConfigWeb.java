package com.flyway.security.config;

import com.flyway.auth.service.AuthTokenService;
import com.flyway.auth.repository.RefreshTokenRepository;
import com.flyway.auth.util.TokenHasher;
import com.flyway.security.filter.OnboardingAccessFilter;
import com.flyway.security.filter.OriginRefererCheckFilter;
import com.flyway.security.filter.RefreshTokenSessionSyncFilter;
import com.flyway.security.handler.JwtAuthenticationEntryPoint;
import com.flyway.security.handler.LoginSuccessHandler;
import com.flyway.security.handler.WebLoginRedirectEntryPoint;
import com.flyway.security.jwt.JwtProvider;
import com.flyway.security.jwt.JwtWebAuthFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Configuration
@Order(3)
public class SecurityConfigWeb extends WebSecurityConfigurerAdapter {

    private static final String[] STATIC_RESOURCES = {
            "/resources/**", "/css/**", "/js/**", "/images/**", "/favicon.ico"
    };

    private static final String[] PUBLIC_ENDPOINTS = {
            "/", "/login", "/loginProc", "/signup", "/auth/**", "/search/**",
            "/payments/success", "/payments/fail", "/payments/complete", "/api/sms/**", "/error", "/error/**",
    };

    private final JwtProvider jwtProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final PasswordEncoder passwordEncoder;
    private final LoginSuccessHandler loginSuccessHandler;
    private final WebLoginRedirectEntryPoint webLoginRedirectEntryPoint;
    private final UserDetailsService userIdUserDetailsService;
    private final UserDetailsService emailUserDetailsService;
    private final AuthTokenService authTokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenHasher tokenHasher;
    private final SecurityOriginProperties securityOriginProperties;

    public SecurityConfigWeb(
            JwtProvider jwtProvider,
            JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
            PasswordEncoder passwordEncoder,
            LoginSuccessHandler loginSuccessHandler,
            WebLoginRedirectEntryPoint webLoginRedirectEntryPoint,
            AuthTokenService authTokenService,
            RefreshTokenRepository refreshTokenRepository,
            TokenHasher tokenHasher,
            SecurityOriginProperties securityOriginProperties,
            @Qualifier("userIdUserDetailsService") UserDetailsService userIdUserDetailsService,
            @Qualifier("emailUserDetailsService") UserDetailsService emailUserDetailsService
    ) {
        this.jwtProvider = jwtProvider;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.passwordEncoder = passwordEncoder;
        this.loginSuccessHandler = loginSuccessHandler;
        this.webLoginRedirectEntryPoint = webLoginRedirectEntryPoint;
        this.authTokenService = authTokenService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.tokenHasher = tokenHasher;
        this.securityOriginProperties = securityOriginProperties;
        this.userIdUserDetailsService = userIdUserDetailsService;
        this.emailUserDetailsService = emailUserDetailsService;
    }

    @Bean
    public JwtWebAuthFilter jwtWebAuthFilter() {
        return new JwtWebAuthFilter(
                jwtProvider,
                jwtAuthenticationEntryPoint,
                userIdUserDetailsService
        );
    }

    @Bean
    public RefreshTokenSessionSyncFilter refreshTokenSessionSyncFilter() {
        List<String> excludes = new ArrayList<>();
        excludes.addAll(Arrays.asList(STATIC_RESOURCES));
        excludes.addAll(Arrays.asList(PUBLIC_ENDPOINTS));
        return new RefreshTokenSessionSyncFilter(
                refreshTokenRepository,
                tokenHasher,
                authTokenService,
                excludes
        );
    }

    @Bean
    public OriginRefererCheckFilter webOriginRefererCheckFilter() {
        return OriginRefererCheckFilter.forWeb(securityOriginProperties.getAllowedOrigins());
    }

    @Bean
    public AuthenticationFailureHandler loginFailureHandler() {
        return (req, res, ex) -> {
            if (ex instanceof BadCredentialsException) {
                log.warn("[LOGIN FAILED] bad credentials. uri={}", req.getRequestURI());
            } else {
                log.error("[LOGIN FAILED] unexpected error. uri={}", req.getRequestURI(), ex);
            }
            res.sendRedirect(req.getContextPath() + "/login?error");
        };
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .userDetailsService(emailUserDetailsService)
                .passwordEncoder(passwordEncoder);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf()
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .and()

                .authorizeRequests()
                .antMatchers(STATIC_RESOURCES).permitAll()
                .antMatchers(PUBLIC_ENDPOINTS).permitAll().anyRequest().authenticated()
                .and()

                .exceptionHandling()
                .authenticationEntryPoint(webLoginRedirectEntryPoint)
                .and()

                .formLogin()
                .loginPage("/login")
                .loginProcessingUrl("/loginProc")
                .failureHandler(loginFailureHandler())
                .usernameParameter("username")
                .passwordParameter("password")
                .successHandler(loginSuccessHandler)
                .permitAll()
                .and()

                .logout()
                .logoutUrl("/auth/logout")
                .addLogoutHandler(jwtCookieLogoutHandler())
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .logoutSuccessUrl("/login")
                .permitAll()
                .and()

                .addFilterBefore(webOriginRefererCheckFilter(), CsrfFilter.class)
                .addFilterBefore(jwtWebAuthFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(refreshTokenSessionSyncFilter(), JwtWebAuthFilter.class)
                .addFilterAfter(new OnboardingAccessFilter(), RefreshTokenSessionSyncFilter.class);
    }

    @Bean
    public LogoutHandler jwtCookieLogoutHandler() {
        return (request, response, authentication) -> authTokenService.logout(request, response);
    }
}
