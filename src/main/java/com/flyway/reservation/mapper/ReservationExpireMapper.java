package com.flyway.reservation.mapper;

import com.flyway.reservation.dto.ExpiredReservationView;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReservationExpireMapper {

    // 만료된 HELD 예약 + segment 정보 조회
    List<ExpiredReservationView> selectExpiredHeldReservations();

    // 잔여석 복구
    int incrementSeat(@Param("flightId") String flightId,
                      @Param("cabinClass") String cabinClass,
                      @Param("count") int count);

    // 예약 상태 변경
    int updateReservationStatus(@Param("reservationId") String reservationId,
                                @Param("status") String status);
}