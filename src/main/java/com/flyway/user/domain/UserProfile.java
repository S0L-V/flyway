package com.flyway.user.domain;

import lombok.*;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserProfile {
    private String userId;
    private String name; // 한글 이름
    private String passportNo;
    private String country; // 국적
    private String gender; // M | F
    private String firstName; // 영문 이름
    private String lastName; // 영문 성
}
