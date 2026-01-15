package com.flyway.reservation.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationSegmentInsertDTO {

    private String reservationSegmentId;
    private String flightId;
    private String reservationId;
    private int segmentOrder;
    private String snapDepartureAirport;
    private String snapArrivalAirport;
    private LocalDateTime snapDepartureTime;
    private LocalDateTime snapArrivalTime;
    private String snapFlightNumber;
    private String snapCabinClassCode;

}
