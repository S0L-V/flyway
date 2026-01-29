package com.flyway.passenger.service;

import com.flyway.passenger.dto.PassengerReservationDto;
import com.flyway.passenger.dto.ReservationPassengersResponseDto;
import com.flyway.passenger.repository.PassengerQueryRepository;
import com.flyway.template.exception.BusinessException;
import com.flyway.template.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class PassengerQueryServiceImpl implements PassengerQueryService {

    private final PassengerQueryRepository passengerQueryRepository;

    @Override
    public ReservationPassengersResponseDto getReservationPassengers(String reservationId) {
        if (!StringUtils.hasText(reservationId)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        List<PassengerReservationDto> rows = passengerQueryRepository.findByReservationId(reservationId);
        if (rows == null || rows.isEmpty()) {
            throw new BusinessException(ErrorCode.RESERVATION_NOT_FOUND);
        }

        return ReservationPassengersResponseDto.fromRows(rows);
    }
}
