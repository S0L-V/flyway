package com.flyway.passenger.mapper;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PassengerPassportMapper {

    int updatePassengerPassport(
            @Param("passengerId") String passengerId,
            @Param("reservationId") String reservationId,
            @Param("userId") String userId,
            @Param("passportNo") String passportNo,
            @Param("issueCountry") String issueCountry,
            @Param("expiryDate") LocalDate expiryDate,
            @Param("country") String country,
            @Param("status") String status,
            @Param("submittedAt") LocalDateTime submittedAt
    );
}
