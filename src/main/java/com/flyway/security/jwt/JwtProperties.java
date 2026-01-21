package com.flyway.security.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class JwtProperties {
    @Value("${jwt.user.secret}")
    private String secret;

    @Value("${jwt.user.accessTokenTtlSeconds}")
    private long accessTokenTtlSeconds;

    @Value("${jwt.user.refreshTokenTtlSeconds}")
    private long refreshTokenTtlSeconds;

    @Value("${jwt.user.issuer}")
    private String issuer;
}
