package com.flyway.reservation.repository;

import com.flyway.reservation.dto.ReservationPaymentDetailDto;
import com.flyway.reservation.mapper.ReservationPaymentQueryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReservationPaymentQueryRepositoryImpl implements ReservationPaymentQueryRepository {

    private final ReservationPaymentQueryMapper reservationPaymentQueryMapper;

    @Override
    public Optional<ReservationPaymentDetailDto> findReservationPaymentDetail(String reservationId) {
        return Optional.ofNullable(reservationPaymentQueryMapper.findReservationPaymentDetail(reservationId));
    }
}
