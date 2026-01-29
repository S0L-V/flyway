package com.flyway.passenger.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface PassengerPassportRepository {

    int updatePassport(
            String passengerId,
            String reservationId,
            String userId,
            String passportNo,
            String issueCountry,
            LocalDate expiryDate,
            String country,
            String status,
            LocalDateTime submittedAt
    );
}
