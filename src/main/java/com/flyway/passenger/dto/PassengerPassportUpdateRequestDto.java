package com.flyway.passenger.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PassengerPassportUpdateRequestDto {
    private String passportNo;
    private String issueCountry;
    private LocalDate expiryDate;
    private String country;
}
