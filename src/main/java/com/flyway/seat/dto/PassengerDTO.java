package com.flyway.seat.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PassengerDTO {
    private String passengerId;
    private String reservationId;

    private String krFirstName;
    private String krLastName;

    private String firstName;
    private String lastName;
}
