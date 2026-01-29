package com.flyway.reservation.dto;

import java.util.List;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationPaymentResponseDto {

    private Fare fare;
    private List<ServiceItem> services;
    private Long totalAmount;
    private Long paidAmount;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Fare {
        private FareSegment outbound;
        private FareSegment inbound; // nullable (편도)
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FareSegment {
        private String flightNumber;
        private String cabinClass;
        private Long pricePerPerson;
        private Integer passengerCount;
        private Long total;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ServiceItem {
        private String name;   // 위탁 수하물 | 기내식
        private Long amount;
    }
}
