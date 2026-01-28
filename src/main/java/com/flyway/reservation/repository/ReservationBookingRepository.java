package com.flyway.reservation.repository;

import com.flyway.reservation.dto.*;

import java.util.List;

public interface ReservationBookingRepository {

    BookingViewModel findReservationHeader(String reservationId);

    BookingViewModel findReservationHeaderByUser(String reservationId, String userId);

    List<ReservationSegmentView> findSegments(String reservationId);

    int countPassengers(String reservationId);

    ReservationCoreView lockReservationForUpdate(String reservationId);

    List<PassengerView> findPassengers(String reservationId);

    int insertPassenger(PassengerUpsertDTO dto);

    int updatePassenger(PassengerUpsertDTO dto);

    int updateReservationStatus(String reservationId, String status);
}
