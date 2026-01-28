package com.flyway.payment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate 빈 설정
 *
 * RestTemplate은 HTTP 호출을 위한 스프링 유틸리티입니다.
 * 타임아웃 설정을 해두면 토스 API가 응답 안 할 때 무한 대기 방지됩니다.
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

        // 연결 타임아웃: 5초 (토스 서버에 연결하는 시간)
        factory.setConnectTimeout(5000);

        // 읽기 타임아웃: 30초 (응답 대기 시간)
        factory.setReadTimeout(30000);

        return new RestTemplate(factory);
    }
}