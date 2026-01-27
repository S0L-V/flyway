package com.flyway.reservation.service;

import com.flyway.reservation.domain.FlightSnapshot;
import com.flyway.reservation.dto.FlightInfoView;
import com.flyway.reservation.dto.ReservationInsertDTO;
import com.flyway.reservation.dto.ReservationSegmentInsertDTO;
import com.flyway.reservation.repository.ReservationDraftRepository;
import com.flyway.reservation.domain.DraftCreateRequest;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReservationDraftService {

    private final ReservationDraftRepository draftRepository;

    @Transactional
    public String createDraft(String userId, DraftCreateRequest req) {

        // 기본 검증
        if (req.getOutFlightId() == null || req.getOutFlightId().isBlank()) {
            throw new IllegalArgumentException("outFlightId is required");
        }
        if (req.getPassengerCount() <= 0) {
            throw new IllegalArgumentException("passengerCount must be >= 1");
        }
        if (req.getCabinClassCode() == null || req.getCabinClassCode().isBlank()) {
            throw new IllegalArgumentException("cabinClassCode is required");
        }

        boolean isRoundTrip = (req.getInFlightId() != null && !req.getInFlightId().isBlank());
        String tripType = isRoundTrip ? "1" : "0";

        // ========== 잔여석 처리 (가는편) ==========
        FlightInfoView outInfo = draftRepository.lockFlightInfoForUpdate(req.getOutFlightId());
        if (outInfo == null) {
            throw new IllegalArgumentException("flight_info not found: " + req.getOutFlightId());
        }

        int outRemaining = getSeatCount(outInfo, req.getCabinClassCode());
        if (outRemaining < req.getPassengerCount()) {
            throw new IllegalStateException("잔여석 부족 (가는편): 남은 좌석 " + outRemaining + "석");
        }

        int outUpdated = draftRepository.decrementSeat(req.getOutFlightId(), req.getCabinClassCode(),
                req.getPassengerCount());
        if (outUpdated == 0) {
            throw new IllegalStateException("잔여석 차감 실패 (가는편): 좌석 부족 또는 유효하지 않은 좌석등급");
        }

        // ========== 잔여석 처리 (오는편 - 왕복인 경우) ==========
        if (isRoundTrip) {
            FlightInfoView inInfo = draftRepository.lockFlightInfoForUpdate(req.getInFlightId());
            if (inInfo == null) {
                throw new IllegalArgumentException("flight_info not found: " + req.getInFlightId());
            }

            int inRemaining = getSeatCount(inInfo, req.getCabinClassCode());
            if (inRemaining < req.getPassengerCount()) {
                throw new IllegalStateException("잔여석 부족 (오는편): 남은 좌석 " + inRemaining + "석");
            }

            int inUpdated = draftRepository.decrementSeat(req.getInFlightId(), req.getCabinClassCode(),
                    req.getPassengerCount());
            if (inUpdated == 0) {
                throw new IllegalStateException("잔여석 차감 실패 (오는편): 좌석 부족 또는 유효하지 않은 좌석등급");
            }
        }

        // ========== 예약 생성 ==========
        String reservationId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiredAt = now.plusMinutes(10);

        draftRepository.saveReservation(
                ReservationInsertDTO.builder()
                        .reservationId(reservationId)
                        .userId(userId)
                        .reservedAt(now)
                        .status("HELD")
                        .passengerCount(req.getPassengerCount())
                        .tripType(tripType)
                        .expiredAt(expiredAt)
                        .build()
        );

        // ========== 가는편 segment 저장 ==========
        FlightSnapshot outSnap = draftRepository.findFlightSnapshot(req.getOutFlightId());
        if (outSnap == null) {
            throw new IllegalArgumentException("outFlightId not found: " + req.getOutFlightId());
        }

        draftRepository.saveReservationSegment(
                ReservationSegmentInsertDTO.builder()
                        .reservationSegmentId(UUID.randomUUID().toString())
                        .flightId(req.getOutFlightId())
                        .reservationId(reservationId)
                        .segmentOrder(1)
                        .snapDepartureAirport(outSnap.getDepartureAirport())
                        .snapArrivalAirport(outSnap.getArrivalAirport())
                        .snapDepartureTime(outSnap.getDepartureTime())
                        .snapArrivalTime(outSnap.getArrivalTime())
                        .snapFlightNumber(outSnap.getFlightNumber())
                        .snapCabinClassCode(req.getCabinClassCode())
                        .snapPrice(req.getOutPrice())
                        .build()
        );

        // ========== 오는편 segment 저장 (왕복) ==========
        if (isRoundTrip) {
            FlightSnapshot inSnap = draftRepository.findFlightSnapshot(req.getInFlightId());
            if (inSnap == null) {
                throw new IllegalArgumentException("inFlightId not found: " + req.getInFlightId());
            }

            draftRepository.saveReservationSegment(
                    ReservationSegmentInsertDTO.builder()
                            .reservationSegmentId(UUID.randomUUID().toString())
                            .flightId(req.getInFlightId())
                            .reservationId(reservationId)
                            .segmentOrder(2)
                            .snapDepartureAirport(inSnap.getDepartureAirport())
                            .snapArrivalAirport(inSnap.getArrivalAirport())
                            .snapDepartureTime(inSnap.getDepartureTime())
                            .snapArrivalTime(inSnap.getArrivalTime())
                            .snapFlightNumber(inSnap.getFlightNumber())
                            .snapCabinClassCode(req.getCabinClassCode())
                            .snapPrice(req.getInPrice())
                            .build()
            );
        }

        return reservationId;
    }

    // 좌석등급별 잔여석 조회 헬퍼
    private int getSeatCount(FlightInfoView info, String cabinClass) {
        switch (cabinClass) {
            case "FST": return info.getFirstClassSeat() != null ? info.getFirstClassSeat() : 0;
            case "BIZ": return info.getBusinessClassSeat() != null ? info.getBusinessClassSeat() : 0;
            case "ECO": return info.getEconomyClassSeat() != null ? info.getEconomyClassSeat() : 0;
            default: return 0;
        }

    }
}
