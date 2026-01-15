package com.flyway.reservation.service;

import com.flyway.reservation.domain.FlightSnapshot;
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
                        .build()
        );

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
                            .build()
            );
        }

        return reservationId;
    }
}
