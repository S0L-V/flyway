package com.flyway.payment.mapper;

import com.flyway.payment.dto.PaymentDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PaymentQueryMapper {

    /**
     * 예약별 결제 조회 (최근 PAID)
     */
    PaymentDto findLatestPaidByReservationId(
            @Param("reservationId") String reservationId
    );
}
