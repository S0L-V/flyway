package com.flyway.passenger.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BaggageSaveRequest {

    private List<PassengerBaggage> items;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PassengerBaggage {

        private String passengerId;
        private String reservationSegmentId;
        private int extraWeightKg;
        private int extraBagCount;

    }

}
