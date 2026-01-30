package com.flyway.reservation.repository;

import com.flyway.reservation.dto.ExpiredReservationView;
import java.util.List;

public interface ReservationExpireRepository {

    List<ExpiredReservationView> findExpiredHeldReservations();

    int incrementSeat(String flightId, String cabinClass, int count);

    int updateReservationStatus(String reservationId, String status);
}