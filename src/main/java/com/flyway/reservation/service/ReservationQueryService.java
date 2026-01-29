package com.flyway.reservation.service;

import com.flyway.reservation.dto.ReservationDetailDto;
import com.flyway.reservation.dto.ReservationSummaryDto;
import com.flyway.template.common.PageResult;

public interface ReservationQueryService {

    /**
     * 유저 예약 내역 목록 조회
     */
    PageResult<ReservationSummaryDto> getUserReservationHistories(
            String userId,
            Integer page, Integer size
    );

    /**
     * 유저 예약 상세 조회
     */
    ReservationDetailDto getUserReservationDetail(String userId, String reservationId);
}
