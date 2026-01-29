package com.flyway.passenger.domain;

import com.flyway.passenger.dto.ReservationPassengersResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PassengerProfile {
    private String krFirstName;
    private String krLastName;
    private String firstName;
    private String lastName;
    private LocalDate birth;
    private String gender;
    private String email;
    private String phoneNumber;
    private String checkedBaggageApplied;
    private PassportInfo passport;
}