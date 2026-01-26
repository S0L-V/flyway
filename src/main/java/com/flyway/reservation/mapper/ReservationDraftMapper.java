package com.flyway.reservation.mapper;

import com.flyway.reservation.domain.FlightSnapshot;
import com.flyway.reservation.dto.FlightInfoView;
import com.flyway.reservation.dto.ReservationInsertDTO;
import com.flyway.reservation.dto.ReservationSegmentInsertDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


import java.time.LocalDateTime;
@Mapper
public interface ReservationDraftMapper {

    FlightSnapshot selectFlightSnapshot(@Param("flightId") String flightId);

    int insertReservation(ReservationInsertDTO param);

    int insertReservationSegment(ReservationSegmentInsertDTO param);

    FlightInfoView lockFlightInfoForUpdate(@Param("flightId") String flightId);

    int decrementSeat(@Param("flightId") String flightId,
                      @Param("cabinClass") String cabinClass,
                      @Param("count") int count);


}
