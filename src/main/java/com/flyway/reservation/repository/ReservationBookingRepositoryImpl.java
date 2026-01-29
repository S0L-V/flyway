package com.flyway.reservation.repository;

import com.flyway.reservation.dto.*;
import com.flyway.reservation.mapper.ReservationBookingMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ReservationBookingRepositoryImpl implements ReservationBookingRepository {

    private final ReservationBookingMapper bookingMapper;

    @Override
    public BookingViewModel findReservationHeader(String reservationId) {
        return bookingMapper.selectReservationHeader(reservationId);
    }

    @Override
    public BookingViewModel findReservationHeaderByUser(String reservationId, String userId) {
        return bookingMapper.selectReservationHeaderByUser(reservationId, userId);
    }

    @Override
    public List<ReservationSegmentView> findSegments(String reservationId) {
        return bookingMapper.selectReservationSegments(reservationId);
    }

    @Override
    public int countPassengers(String reservationId) {
        Integer cnt = bookingMapper.countPassengers(reservationId);
        return (cnt == null) ? 0 : cnt;
    }

    @Override
    public ReservationCoreView lockReservationForUpdate(String reservationId) {
        return bookingMapper.lockReservationForUpdate(reservationId);
    }

    @Override
    public List<PassengerView> findPassengers(String reservationId) {
        return bookingMapper.selectPassengers(reservationId);
    }

    @Override
    public int insertPassenger(PassengerUpsertDTO dto) {
        return bookingMapper.insertPassenger(dto);
    }

    @Override
    public int updatePassenger(PassengerUpsertDTO dto) {
        return bookingMapper.updatePassenger(dto);
    }

    @Override
    public int updateReservationStatus(String reservationId, String status) {
        return bookingMapper.updateReservationStatus(reservationId, status);
    }
    @Override
    public List<PassengerSeatInfo> findPassengerSeatsBySegment(String reservationSegmentId) {
        return bookingMapper.selectPassengerSeatsBySegment(reservationSegmentId);
    }

    @Override
    public List<PassengerServiceInfo> findPassengerServicesBySegment(String reservationSegmentId) {
        return bookingMapper.selectPassengerServicesBySegment(reservationSegmentId);
    }
}
