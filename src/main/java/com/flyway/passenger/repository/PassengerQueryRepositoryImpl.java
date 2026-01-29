package com.flyway.passenger.repository;

import com.flyway.passenger.dto.PassengerReservationDto;
import java.util.List;

import com.flyway.passenger.mapper.PassengerQueryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PassengerQueryRepositoryImpl implements PassengerQueryRepository {

    private final PassengerQueryMapper passengerQueryMapper;

    @Override
    public List<PassengerReservationDto> findByReservationId(String reservationId, String userId) {
        return passengerQueryMapper.selectPassengersByReservationId(reservationId, userId);
    }
}
