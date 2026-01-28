package com.flyway.reservation.repository;

import com.flyway.reservation.dto.ReservationSummaryDto;
import com.flyway.template.common.PageResult;
import com.flyway.template.common.Paging;

public interface ReservationQueryRepository {

    /**
     * 유저 예약 내역 목록 조회
     */
    PageResult<ReservationSummaryDto> findUserReservationHistories(
            String userId,
            Paging paging
    );
}
