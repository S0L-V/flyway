package com.flyway.passenger.service;

import com.flyway.passenger.dto.PassengerPassportUpdateRequestDto;

public interface PassengerPassportService {

    void updatePassport(String userId, String reservationId, String passengerId, PassengerPassportUpdateRequestDto request);
}
