package com.flyway.reservation.mapper;

import com.flyway.reservation.dto.ReservationDetailDto;
import com.flyway.reservation.dto.ReservationSummaryDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReservationQueryMapper {

    /**
     * 회원 예약 목록 조회
     */
    List<ReservationSummaryDto> findReservationHistoriesByUserId(
            @Param("userId") String userId,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    /**
     * 회원 예약 상세 조회
     */
    ReservationDetailDto findReservationDetail(
            @Param("userId") String userId,
            @Param("reservationId") String reservationId
    );

    int countReservationHistoriesByUserId(
            @Param("userId") String userId
    );
}
