package com.flyway.passenger.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.stereotype.Repository;

import com.flyway.passenger.mapper.PassengerPassportMapper;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PassengerPassportRepositoryImpl implements PassengerPassportRepository {

    private final PassengerPassportMapper passengerPassportMapper;

    @Override
    public int updatePassport(
            String passengerId,
            String reservationId,
            String userId,
            String passportNo,
            String issueCountry,
            LocalDate expiryDate,
            String country,
            String status,
            LocalDateTime submittedAt
    ) {
        return passengerPassportMapper.updatePassengerPassport(
                passengerId,
                reservationId,
                userId,
                passportNo,
                issueCountry,
                expiryDate,
                country,
                status,
                submittedAt
        );
    }
}
