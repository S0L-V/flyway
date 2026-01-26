package com.flyway.auth.client;

import com.flyway.auth.config.KakaoProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class KakaoUnlinkClient {

    private static final String UNLINK_URL = "https://kapi.kakao.com/v1/user/unlink";

    private final RestTemplate restTemplate = new RestTemplate();
    private final KakaoProperties kakaoProperties;

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

        ResponseEntity<String> res = restTemplate.exchange(
                UNLINK_URL,
                HttpMethod.POST,
                entity,
                String.class
        );

        if (!res.getStatusCode().is2xxSuccessful()) {
            log.warn("kakao unlink failed. status={}, body={}", res.getStatusCode(), res.getBody());
        } else {
            log.info("kakao unlink success. kakaoUserId={}", kakaoUserId);
        }
    }
}
