package com.flyway.reservation.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PassengerView {

    private String passengerId;
    private String krFirstName;
    private String krLastName;
    private String firstName;
    private String lastName;
    private LocalDate birth;     // DATE
    private String gender;       // M/F
    private String email;
    private String phoneNumber;

    private String passportNo;
    private String country;
    private LocalDate passportExpiryDate;
    private String passportIssueCountry;
}
