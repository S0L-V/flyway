package com.flyway.security.config;

import com.flyway.security.handler.JwtAccessDeniedHandler;
import com.flyway.security.handler.JwtAuthenticationEntryPoint;
import com.flyway.security.filter.OnboardingAccessFilter;
import com.flyway.security.jwt.JwtApiAuthFilter;
import com.flyway.security.jwt.JwtProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@Order(1)
public class SecurityConfigApi extends WebSecurityConfigurerAdapter {

    private final JwtProvider jwtProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final UserDetailsService userIdUserDetailsService;

    public SecurityConfigApi(
            JwtProvider jwtProvider,
            JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
            JwtAccessDeniedHandler jwtAccessDeniedHandler,
            @Qualifier("userIdUserDetailsService") UserDetailsService userIdUserDetailsService
    ) {
        this.jwtProvider = jwtProvider;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
        this.userIdUserDetailsService = userIdUserDetailsService;
    }

    @Bean
    public JwtApiAuthFilter jwtApiAuthFilter() {
        return new JwtApiAuthFilter(
                jwtProvider,
                jwtAuthenticationEntryPoint,
                userIdUserDetailsService
        );
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .antMatcher("/api/**")
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()

                .csrf().disable()
                .formLogin().disable()
                .httpBasic().disable()

                .exceptionHandling()
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(jwtAccessDeniedHandler)
                .and()

                .authorizeHttpRequests()

                .antMatchers(HttpMethod.OPTIONS, "/api/**").permitAll() //CORS Preflight 허용

                // 비회원 허용 API
                .antMatchers(
                        "/api/auth/**",
                        "/api/public/**"
                ).permitAll()

                .anyRequest().authenticated()
                .and()

                .addFilterBefore(jwtApiAuthFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(new OnboardingAccessFilter(), JwtApiAuthFilter.class);
    }
}
