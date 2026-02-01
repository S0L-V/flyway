package com.flyway.pricing.model;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRepriceSegment {
    private String flightId;
    private int segmentOrder;

    private String cabinClassCode;      // snap_cabin_class_code
    private LocalDateTime departureTime; // snap_departure_time
}