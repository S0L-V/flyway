package com.flyway.auth.client;

import com.flyway.auth.config.KakaoProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class KakaoUnlinkClient {

    private static final String UNLINK_URL = "https://kapi.kakao.com/v1/user/unlink";

    private final RestTemplate restTemplate;
    private final KakaoProperties kakaoProperties;

    public KakaoUnlinkClient(RestTemplate externalApiRestTemplate, KakaoProperties kakaoProperties) {
        this.restTemplate = externalApiRestTemplate;
        this.kakaoProperties = kakaoProperties;
    }

    /**
     * Admin Key, kakaoUserId 통하여 카카오 연동 해제
     */
    public void unlinkByKakaoUserId(String kakaoUserId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "KakaoAK " + kakaoProperties.getKakaoAdminKey());

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("target_id_type", "user_id");
        body.add("target_id", kakaoUserId);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<String> res = restTemplate.exchange(
                    UNLINK_URL,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (!res.getStatusCode().is2xxSuccessful()) {
                log.warn("kakao unlink failed. status={}, body={}", res.getStatusCode(), res.getBody());
                return;
            }

            log.info("kakao unlink success. kakaoUserId={}", kakaoUserId);
        } catch (RestClientResponseException ex) {
            log.warn("kakao unlink failed. status={}", ex.getRawStatusCode());
        } catch (RestClientException ex) {
            log.warn("kakao unlink failed. error={}", ex.getMessage(), ex);
        }

    }
}
