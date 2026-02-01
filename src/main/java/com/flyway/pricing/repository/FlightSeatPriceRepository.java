package com.flyway.pricing.repository;

import com.flyway.pricing.model.FlightSeatPriceRow;

import java.time.LocalDateTime;

public interface FlightSeatPriceRepository {
    FlightSeatPriceRow lockForUpdate(String flightId, String cabinClassCode);
    int updateAfterEventPricing(String flightId, String cabinClassCode, long newPrice, LocalDateTime now);
}
