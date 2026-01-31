package com.flyway.passenger.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.flyway.passenger.domain.PassengerProfile;
import com.flyway.passenger.domain.PassportInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationPassengersResponseDto {
    private String reservationId;
    private List<PassengerInfo> passengers;

    public static ReservationPassengersResponseDto fromRows(List<PassengerReservationDto> rows) {
        if (rows == null || rows.isEmpty()) {
            return ReservationPassengersResponseDto.builder()
                    .passengers(new ArrayList<>())
                    .build();
        }

        Map<String, PassengerInfoBuilder> passengerMap = new LinkedHashMap<>();
        String reservationId = null;

        for (PassengerReservationDto row : rows) {
            if (reservationId == null) {
                reservationId = row.getReservationId();
            }

            PassengerInfoBuilder pb = passengerMap.computeIfAbsent(
                    row.getPassengerId(),
                    id -> new PassengerInfoBuilder(row)
            );

            pb.addSegment(row);
        }

        List<PassengerInfo> passengerInfos = new ArrayList<>();
        for (PassengerInfoBuilder builder : passengerMap.values()) {
            passengerInfos.add(builder.build());
        }

        return ReservationPassengersResponseDto.builder()
                .reservationId(reservationId)
                .passengers(passengerInfos)
                .build();
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PassengerInfo {
        private String passengerId;
        private PassengerProfile profile;
        private List<SegmentInfo> segments;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SegmentInfo {
        private String reservationSegmentId;
        private Integer segmentOrder;
        private SeatInfo seat;
        private List<ServiceInfo> services;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SeatInfo {
        private String passengerSeatId;
        private String flightSeatId;
        private String seatNo;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ServiceInfo {
        private String psId;
        private String serviceType;
        private String tripType;
        private Integer quantity;
        private Long totalPrice;
        private String mealId;
        private String mealName;
        private String policyId;
        private String serviceDetails;
        private LocalDateTime addedAt;
    }

    private static class PassengerInfoBuilder {
        private final String passengerId;
        private final PassengerProfile profile;
        private final Map<String, SegmentInfoBuilder> segmentMap = new LinkedHashMap<>();

        private PassengerInfoBuilder(PassengerReservationDto row) {
            this.passengerId = row.getPassengerId();
            this.profile = PassengerProfile.builder()
                    .krFirstName(row.getKrFirstName())
                    .krLastName(row.getKrLastName())
                    .firstName(row.getFirstName())
                    .lastName(row.getLastName())
                    .birth(row.getBirth())
                    .gender(row.getGender())
                    .email(row.getEmail())
                    .phoneNumber(row.getPhoneNumber())
                    .checkedBaggageApplied(row.getCheckedBaggageApplied())
                    .passport(PassportInfo.builder()
                            .passportNo(row.getPassportNo())
                            .issueCountry(row.getPassportIssueCountry())
                            .expiryDate(row.getPassportExpiryDate())
                            .status(row.getPassportStatus())
                            .submittedAt(row.getPassportSubmittedAt())
                            .reminderSentAt(row.getPassportReminderSentAt())
                            .build())
                    .build();
        }

        private void addSegment(PassengerReservationDto row) {
            if (row.getReservationSegmentId() == null) {
                return;
            }
            SegmentInfoBuilder sb = segmentMap.computeIfAbsent(
                    row.getReservationSegmentId(),
                    id -> new SegmentInfoBuilder(row)
            );
            sb.addService(row);
        }

        private PassengerInfo build() {
            List<SegmentInfo> segments = new ArrayList<>();
            for (SegmentInfoBuilder builder : segmentMap.values()) {
                segments.add(builder.build());
            }
            return PassengerInfo.builder()
                    .passengerId(passengerId)
                    .profile(profile)
                    .segments(segments)
                    .build();
        }
    }

    private static class SegmentInfoBuilder {
        private final String reservationSegmentId;
        private final Integer segmentOrder;
        private final SeatInfo seat;
        private final List<ServiceInfo> services = new ArrayList<>();

        private SegmentInfoBuilder(PassengerReservationDto row) {
            this.reservationSegmentId = row.getReservationSegmentId();
            this.segmentOrder = row.getSegmentOrder();
            this.seat = SeatInfo.builder()
                    .passengerSeatId(row.getPassengerSeatId())
                    .flightSeatId(row.getFlightSeatId())
                    .seatNo(row.getSeatNo())
                    .build();
        }

        private void addService(PassengerReservationDto row) {
            if (row.getPsId() == null) {
                return;
            }
            services.add(ServiceInfo.builder()
                    .psId(row.getPsId())
                    .serviceType(row.getServiceType())
                    .tripType(row.getTripType())
                    .quantity(row.getQuantity())
                    .totalPrice(row.getTotalPrice())
                    .mealId(row.getMealId())
                    .mealName(row.getMealName())
                    .policyId(row.getPolicyId())
                    .serviceDetails(row.getServiceDetails())
                    .addedAt(row.getAddedAt())
                    .build());
        }

        private SegmentInfo build() {
            return SegmentInfo.builder()
                    .reservationSegmentId(reservationSegmentId)
                    .segmentOrder(segmentOrder)
                    .seat(seat)
                    .services(services)
                    .build();
        }
    }
}
