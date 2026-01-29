package com.flyway.passenger.service;

import com.flyway.passenger.dto.ReservationPassengersResponseDto;

public interface PassengerQueryService {

    /**
     * 탑승객별 예약 정보 (개인정보, 여권, 좌석, 부가서비스)
     */
    ReservationPassengersResponseDto getReservationPassengers(String reservationId);
}
