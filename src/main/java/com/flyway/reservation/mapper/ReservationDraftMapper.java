package com.flyway.reservation.mapper;

import com.flyway.reservation.domain.FlightSnapshot;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

public interface ReservationDraftMapper {

    FlightSnapshot selectFlightSnapshot(@Param("flightId") String flightId);

    int insertReservation (
            @Param("reservationId") String reservationId,
            @Param("userId") String userId,
            @Param("reservedAt")LocalDateTime reservedAt,
            @Param("passengerCount") int passengerCount,
            @Param("tripType") String tripType,
            @Param("expiredAt") LocalDateTime expiredAt
            );

    int insertReservationSegment (
            @Param("reservationSegmentId") String reservationSegmentId,
            @Param("flightId")  String flightId,
            @Param("segmentOrder")  String segmentOrder,
            @Param("snapDepartureAirport") String snapDepartureAirport,
            @Param("snapArrivalAirport") String snapArrivalAirport,
            @Param("snapDepartureTime") String snapDepartureTime,
            @Param("snapArrivalTime") String snapArrivalTime,
            @Param("snapFlightNumber")  String snapFlightNumber,
            @Param("snapCabinClassCode") String snapCabinClassCode
    );

}
