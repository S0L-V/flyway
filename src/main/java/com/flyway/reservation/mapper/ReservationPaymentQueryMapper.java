package com.flyway.reservation.mapper;

import com.flyway.reservation.dto.ReservationPaymentDetailDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ReservationPaymentQueryMapper {

    /**
     * 예약 결제 상세 조회
     */
    ReservationPaymentDetailDto findReservationPaymentDetail(@Param("reservationId") String reservationId);
}
