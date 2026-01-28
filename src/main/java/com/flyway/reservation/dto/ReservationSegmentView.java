package com.flyway.reservation.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationSegmentView {

    private String reservationSegmentId;
    private int segmentOrder;

    private String snapDepartureAirport;
    private String snapArrivalAirport;
    private LocalDateTime snapDepartureTime;
    private LocalDateTime snapArrivalTime;
    private String snapFlightNumber;
    private String snapCabinClassCode;

    private Long snapPrice;
    private String snapDepartureCity;   // 출발 도시명 (인천)
    private String snapArrivalCity;     // 도착 도시명 (나리타)
    private String snapAirlineName;     // 항공사명
}
