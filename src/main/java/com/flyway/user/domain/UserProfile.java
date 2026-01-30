package com.flyway.user.domain;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Getter
@Setter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
public class UserProfile {
    private String userId;
    private String name; // 한글 이름 (full name)
    private String krFirstName;
    private String krLastName;
    private LocalDate birth;
    private String email;
    private String phoneNumber;
    private String passportNo;
    private LocalDate passportExpiryDate;
    private String passportIssueCountry;
    private String country; // 국적
    private String gender; // M | F
    private String firstName; // 영문 이름
    private String lastName; // 영문 성
}
