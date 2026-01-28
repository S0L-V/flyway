package com.flyway.payment.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@Setter
public class TossPaymentsConfig {

    @Value("${toss.payments.client-key}")
    private String clientKey;

    @Value("${toss.payments.secret-key}")
    private String secretKey;

    @Value("${toss.payments.api-url}")
    private String apiUrl;

    @Value("${toss.payments.success-url}")
    private String successUrl;

    @Value("${toss.payments.fail-url}")
    private String failUrl;

}
