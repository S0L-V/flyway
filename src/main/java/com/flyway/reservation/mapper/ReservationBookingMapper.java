package com.flyway.reservation.mapper;

import com.flyway.passenger.dto.PassengerUpsertDTO;
import com.flyway.passenger.dto.PassengerView;
import com.flyway.reservation.dto.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReservationBookingMapper {

    BookingViewModel selectReservationHeader(@Param("reservationId") String reservationId);

    //  본인 예약만 조회
    BookingViewModel selectReservationHeaderByUser(
            @Param("reservationId") String reservationId,
            @Param("userId") String userId
    );

    List<ReservationSegmentView> selectReservationSegments(@Param("reservationId") String reservationId);

    Integer countPassengers(@Param("reservationId") String reservationId);

    // 예약 잠금(동시 저장 충돌 )
    ReservationCoreView lockReservationForUpdate(@Param("reservationId") String reservationId);

    // 탑승자 조회
    List<PassengerView> selectPassengers(@Param("reservationId") String reservationId);

    // 탑승자 저장
    int insertPassenger(PassengerUpsertDTO dto);
    int updatePassenger(PassengerUpsertDTO dto);

    int updateReservationStatus(@Param("reservationId") String reservationId,
                                @Param("status") String status);

    // 구간별 탑승자 좌석 조회
    List<PassengerSeatInfo> selectPassengerSeatsBySegment(
            @Param("reservationSegmentId") String reservationSegmentId
    );

    // 구간별 탑승자 부가서비스 조회 (수하물 + 기내식)
    List<PassengerServiceInfo> selectPassengerServicesBySegment(
            @Param("reservationSegmentId") String reservationSegmentId
    );
}
