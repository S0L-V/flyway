package com.flyway.pricing.repository;

import com.flyway.pricing.mapper.FlightSeatPriceMapper;
import com.flyway.pricing.model.FlightSeatPriceRow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
@RequiredArgsConstructor
public class FlightSeatPriceRepositoryImpl implements FlightSeatPriceRepository{

    private final FlightSeatPriceMapper flightSeatPriceMapper;

    public FlightSeatPriceRow lockForUpdate(String flightId, String cabinClassCode) {
        return flightSeatPriceMapper.selectForUpdate(flightId, cabinClassCode); // FOR UPDATE
    }

    public int updateAfterEventPricing(String flightId, String cabinClassCode, long newPrice, LocalDateTime now) {
        return flightSeatPriceMapper.updateAfterEventPricing(flightId, cabinClassCode, newPrice, now);
    }
}
