package com.flyway.sender.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
public class SmsConfig {

    @Value("${sms.nurigo.api-key}")
    private String apiKey;

    @Value("${sms.nurigo.api-secret}")
    private String apiSecret;

    @Value("${sms.nurigo.sender}")
    private String sender;
}