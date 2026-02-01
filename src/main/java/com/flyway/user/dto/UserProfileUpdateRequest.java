package com.flyway.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileUpdateRequest {
    private String krFirstName;
    private String krLastName;
    private LocalDate birth;
    private String phoneNumber;
    private String passportNo;
    private LocalDate passportExpiryDate;
    private String passportIssueCountry;
    private String country;
    private String gender; // M | F
    private String firstName;
    private String lastName;
}
