package com.flyway.reservation.repository;

import com.flyway.reservation.domain.FlightSnapshot;
import com.flyway.reservation.dto.FlightInfoView;
import com.flyway.reservation.dto.ReservationInsertDTO;
import com.flyway.reservation.dto.ReservationSegmentInsertDTO;
import com.flyway.reservation.mapper.ReservationDraftMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
@RequiredArgsConstructor
public class ReservationDraftRepositoryImpl implements ReservationDraftRepository {

    private final ReservationDraftMapper draftMapper;

    @Override
    public FlightSnapshot findFlightSnapshot(String flightId) {
        return draftMapper.selectFlightSnapshot(flightId);
    }

    @Override
    public void saveReservation(ReservationInsertDTO param) {
        draftMapper.insertReservation(param);
    }

    @Override
    public void saveReservationSegment(ReservationSegmentInsertDTO param) {
        draftMapper.insertReservationSegment(param);
    }
    @Override
    public FlightInfoView lockFlightInfoForUpdate(String flightId) {
        return draftMapper.lockFlightInfoForUpdate(flightId);
    }
    @Override
    public int decrementSeat(String flightId, String cabinClassCode, int count) {
        return draftMapper.decrementSeat(flightId, cabinClassCode, count);
    }
}
