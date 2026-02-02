package com.flyway.sender.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PassengerSmsInfo {

    private String passengerId;
    private String firstName;
    private String lastName;
    private String phoneNumber;

}
