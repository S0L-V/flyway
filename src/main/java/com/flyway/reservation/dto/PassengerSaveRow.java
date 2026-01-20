package com.flyway.reservation.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PassengerSaveRow {

    private String passengerId; // hidden (있으면 update, 없으면 insert)
    private String krFirstName;
    private String krLastName;
    private String firstName;
    private String lastName;
    private String birth;   // yyyy-MM-dd (문자열 -> 서비스에서 LocalDate )
    private String gender;  // M/F
    private String email;
    private String phoneNumber;

    private String passportNo;
    private String country;
    private String passportExpiryDate;     // yyyy-MM-dd
    private String passportIssueCountry;
}
