package com.flyway.reservation.repository;

import com.flyway.reservation.dto.ExpiredReservationView;
import com.flyway.reservation.mapper.ReservationExpireMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ReservationExpireRepositoryImpl implements ReservationExpireRepository {

    private final ReservationExpireMapper expireMapper;

    @Override
    public List<ExpiredReservationView> findExpiredHeldReservations() {
        return expireMapper.selectExpiredHeldReservations();
    }

    @Override
    public int incrementSeat(String flightId, String cabinClass, int count) {
        return expireMapper.incrementSeat(flightId, cabinClass, count);
    }

    @Override
    public int updateReservationStatus(String reservationId, String status) {
        return expireMapper.updateReservationStatus(reservationId, status);
    }
}