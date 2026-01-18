package com.flyway.security.config;

import com.flyway.security.handler.JwtAuthenticationEntryPoint;
import com.flyway.security.handler.LoginSuccessHandler;
import com.flyway.security.jwt.JwtProvider;
import com.flyway.security.jwt.JwtWebAuthFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Configuration
@Order(2)
@RequiredArgsConstructor
public class SecurityConfigWeb extends WebSecurityConfigurerAdapter {

    private static final String[] STATIC_RESOURCES = {
            "/resources/**", "/css/**", "/js/**", "/images/**", "/favicon.ico"
    };

    private static final String[] PUBLIC_ENDPOINTS = {
            "/", "/login", "/loginProc", "/logout", "/signup", "/admin", "/admin/**", "/auth/**", "/search"
    };

    private final JwtProvider jwtProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final PasswordEncoder passwordEncoder;
    private final LoginSuccessHandler loginSuccessHandler;
    private final UserDetailsService userIdUserDetailsService;
    private final UserDetailsService emailUserDetailsService;

    @Bean
    public JwtWebAuthFilter jwtWebAuthFilter() {
        return new JwtWebAuthFilter(
                jwtProvider,
                jwtAuthenticationEntryPoint,
                userIdUserDetailsService
        );
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
                .csrf().disable()

                .authorizeRequests()
                .antMatchers(STATIC_RESOURCES).permitAll()
                .antMatchers(PUBLIC_ENDPOINTS).permitAll().anyRequest().authenticated()
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
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login")
                .deleteCookies("JSESSIONID", "accessToken")
                .permitAll()
                .and()

                .addFilterBefore(jwtWebAuthFilter(), UsernamePasswordAuthenticationFilter.class);
    }
}

