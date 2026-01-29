package com.flyway.passenger.mapper;

import com.flyway.passenger.dto.PassengerReservationDto;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PassengerQueryMapper {

    /**
     * 탑승객별 예약 정보 (개인정보, 여권, 좌석, 부가서비스)
     */
    List<PassengerReservationDto> selectPassengersByReservationId(
            @Param("reservationId") String reservationId,
            @Param("userId") String userId
    );
}
