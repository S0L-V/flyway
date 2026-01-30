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
    public ReservationPassengersResponseDto getReservationPassengers(String reservationId, String userId) {
        if (!StringUtils.hasText(reservationId)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (!StringUtils.hasText(userId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        List<PassengerReservationDto> rows = passengerQueryRepository.findByReservationId(reservationId, userId);
        if (rows == null || rows.isEmpty()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        return ReservationPassengersResponseDto.fromRows(rows);
    }
}
