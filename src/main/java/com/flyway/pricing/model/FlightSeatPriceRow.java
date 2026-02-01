package com.flyway.pricing.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FlightSeatPriceRow {
    long basePrice;
    long currentPrice;
    int totalSeats;
    int remainingSeats;
    LocalDateTime lastEventPricedAt;
    LocalDateTime departureTime;
}
