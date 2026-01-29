package com.flyway.passenger.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PassportInfo {
    private String passportNo;
    private String country;
    private String issueCountry;
    private LocalDate expiryDate;
    private String status;
    private LocalDateTime submittedAt;
    private LocalDateTime reminderSentAt;
}
