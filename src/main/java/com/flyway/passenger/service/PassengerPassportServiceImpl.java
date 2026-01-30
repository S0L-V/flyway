package com.flyway.passenger.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.flyway.passenger.dto.PassengerPassportUpdateRequestDto;
import com.flyway.passenger.repository.PassengerPassportRepository;
import com.flyway.template.exception.BusinessException;
import com.flyway.template.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PassengerPassportServiceImpl implements PassengerPassportService {

    private final PassengerPassportRepository passengerPassportRepository;

    @Override
    @Transactional
    public void updatePassport(String userId, String reservationId, String passengerId, PassengerPassportUpdateRequestDto request) {
        if (!StringUtils.hasText(userId) || !StringUtils.hasText(reservationId) || !StringUtils.hasText(passengerId)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (request == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        String passportNo = normalize(request.getPassportNo());
        String issueCountry = normalize(request.getIssueCountry());
        LocalDate expiryDate = request.getExpiryDate();
        String country = normalize(request.getCountry());

        if (passportNo == null || issueCountry == null || expiryDate == null || country == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        int updated = passengerPassportRepository.updatePassport(
                passengerId,
                reservationId,
                userId,
                passportNo,
                issueCountry,
                expiryDate,
                country,
                "SUBMIT",
                LocalDateTime.now()
        );

        if (updated == 0) {
            throw new BusinessException(ErrorCode.RESERVATION_NOT_FOUND);
        }
    }

    private String normalize(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
