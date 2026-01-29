package com.flyway.passenger.repository;

import com.flyway.passenger.dto.PassengerReservationDto;
import java.util.List;

public interface PassengerQueryRepository {

    /**
     * 탑승객별 예약 정보 (개인정보, 여권, 좌석, 부가서비스)
     */
    List<PassengerReservationDto> findByReservationId(String reservationId);
}
