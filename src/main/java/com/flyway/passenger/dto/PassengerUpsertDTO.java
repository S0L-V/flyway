package com.flyway.passenger.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PassengerUpsertDTO {

    private String passengerId;
    private String reservationId;
    private String krFirstName;
    private String krLastName;
    private String firstName;
    private String lastName;
    private LocalDate birth;
    private String gender;
    private String email;
    private String phoneNumber;
    private String passportNo;
    private String country;
    private LocalDate passportExpiryDate;
    private String passportIssueCountry;
}