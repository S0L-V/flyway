package com.flyway.pricing.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

public interface SeatAvailabilityService {

    /**
     * 특정 항공편(flightId)의 특정 좌석 등급(cabinClassCode)에 대한
     * 총 좌석 수와 잔여 좌석 수를 조회.
     */
    SeatStatus getSeatStatus(String flightId, String cabinClassCode);

    /**
     * 좌석 현황 DTO
     */
    @Getter
    @AllArgsConstructor
    @ToString
    class SeatStatus {
        private final int totalSeats;
        private final int remainingSeats;
    }
}