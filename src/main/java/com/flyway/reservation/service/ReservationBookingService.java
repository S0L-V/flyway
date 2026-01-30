package com.flyway.reservation.service;

import com.flyway.passenger.dto.PassengerSaveRow;
import com.flyway.passenger.dto.PassengerUpsertDTO;
import com.flyway.passenger.dto.PassengerView;
import com.flyway.reservation.dto.*;
import com.flyway.reservation.repository.ReservationBookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReservationBookingService {

    private final ReservationBookingRepository bookingRepository;

    /*
    public BookingViewModel getBookingView(String reservationId) {
        BookingViewModel header = bookingRepository.findReservationHeader(reservationId);
        if (header == null) {
            throw new IllegalArgumentException("reservation not found: " + reservationId);
        }
        return buildViewModel(header);
    }

     */

    // 로그인 userId 기준으로 본인 예약만 조회
    public BookingViewModel getBookingView(String reservationId, String userId) {
        BookingViewModel header = bookingRepository.findReservationHeaderByUser(reservationId, userId);
        if (header == null) {
            throw new IllegalArgumentException("reservation not found or not yours: " + reservationId);
        }
        return buildViewModel(header);
    }

    private BookingViewModel buildViewModel(BookingViewModel header) {
        String reservationId = header.getReservationId();

        List<ReservationSegmentView> segments = bookingRepository.findSegments(reservationId);

        for (ReservationSegmentView segment : segments) {
            List<PassengerSeatInfo> seats = bookingRepository.findPassengerSeatsBySegment(
                    segment.getReservationSegmentId()
            );
            segment.setPassengerSeats(seats);
            // 부가서비스 (수하물 + 기내식)
            List<PassengerServiceInfo> services = bookingRepository.findPassengerServicesBySegment(
                    segment.getReservationSegmentId()
            );
            segment.setPassengerServices(services);
        }


        int savedPassengerCount = bookingRepository.countPassengers(reservationId);
        boolean passengerSaved = (savedPassengerCount == header.getPassengerCount());

        // 승객 조회 + 인원수만큼 동적 할당
        List<PassengerView> existing = bookingRepository.findPassengers(reservationId);
        List<PassengerView> padded = padPassengers(existing, header.getPassengerCount());

        header.setSegments(segments);
        header.setPassengerSaved(passengerSaved);
        header.setPassengers(padded);

        return header;
    }

    private List<PassengerView> padPassengers(List<PassengerView> existing, int passengerCount) {
        List<PassengerView> result = new ArrayList<>();
        if (existing != null) result.addAll(existing);

        // trim
        if (result.size() > passengerCount) {
            result = result.subList(0, passengerCount);
        }

        // pad blanks
        while (result.size() < passengerCount) {
            result.add(PassengerView.builder().build());
        }
        return result;
    }

    @Transactional
    public void savePassengers(String reservationId, String userId, List<PassengerSaveRow> rows) {

        ReservationCoreView core = bookingRepository.lockReservationForUpdate(reservationId);
        if (core == null) {
            throw new IllegalArgumentException("reservation not found: " + reservationId);
        }

        // 소유자 검증
        if (!Objects.equals(core.getUserId(), userId)) {
            throw new IllegalStateException("forbidden: reservation is not yours");
        }

        // 상태/만료 확인
        if (core.getExpiredAt() != null && core.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("reservation expired");
        }

        // 인원수 검증
        int expected = core.getPassengerCount();
        if (rows == null || rows.size() != expected) {
            throw new IllegalArgumentException("passenger rows must be exactly " + expected);
        }

        for (PassengerSaveRow row : rows) {

            requireNotBlank(row.getKrFirstName(), "krFirstName");
            requireNotBlank(row.getKrLastName(), "krLastName");
            requireNotBlank(row.getFirstName(), "firstName");
            requireNotBlank(row.getLastName(), "lastName");
            requireNotBlank(row.getBirth(), "birth");
            requireNotBlank(row.getGender(), "gender");
            requireNotBlank(row.getEmail(), "email");
            requireNotBlank(row.getPhoneNumber(), "phoneNumber");

            String passengerId = (isBlank(row.getPassengerId()))
                    ? UUID.randomUUID().toString()
                    : row.getPassengerId().trim();

            PassengerUpsertDTO dto = PassengerUpsertDTO.builder()
                    .passengerId(passengerId)
                    .reservationId(reservationId)
                    .krFirstName(row.getKrFirstName())
                    .krLastName(row.getKrLastName())
                    .firstName(row.getFirstName())
                    .lastName(row.getLastName())
                    .birth(parseDate(row.getBirth()))
                    .gender(row.getGender())
                    .email(row.getEmail())
                    .phoneNumber(row.getPhoneNumber())
                    .passportNo(emptyToNull(row.getPassportNo()))
                    .country(emptyToNull(row.getCountry()))
                    .passportExpiryDate(parseDateNullable(row.getPassportExpiryDate()))
                    .passportIssueCountry(emptyToNull(row.getPassportIssueCountry()))
                    .build();

            // upsert
            int updated = bookingRepository.updatePassenger(dto);
            if (updated == 0) {
                bookingRepository.insertPassenger(dto);
            }
        }
    }

    private LocalDate parseDate(String v) {
        try {
            return LocalDate.parse(v.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception e) {
            throw new IllegalArgumentException("invalid date format (yyyy-MM-dd): " + v);
        }
    }

    private LocalDate parseDateNullable(String v) {
        if (isBlank(v)) return null;
        return parseDate(v);
    }

    private void requireNotBlank(String v, String field) {
        if (isBlank(v)) {
            throw new IllegalArgumentException(field + " is required");
        }
    }

    private boolean isBlank(String v) {
        return v == null || v.trim().isEmpty();
    }

    private String emptyToNull(String v) {
        return isBlank(v) ? null : v.trim();
    }


}
