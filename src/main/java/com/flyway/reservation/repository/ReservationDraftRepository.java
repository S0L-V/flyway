package com.flyway.reservation.repository;

import com.flyway.reservation.domain.FlightSnapshot;
import com.flyway.reservation.dto.ReservationInsertDTO;
import com.flyway.reservation.dto.ReservationSegmentInsertDTO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

public interface ReservationDraftRepository {


    FlightSnapshot findFlightSnapshot(String flightId);

    void saveReservation(ReservationInsertDTO param);

    void saveReservationSegment(ReservationSegmentInsertDTO param);

}
