package com.flyway.pricing.mapper;

import com.flyway.pricing.model.FlightSeatPriceRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;

@Mapper
public interface FlightSeatPriceMapper {

    FlightSeatPriceRow selectForUpdate(
            @Param("flightId") String flightId,
            @Param("cabinClassCode") String cabinClassCode
    );

    int updateAfterEventPricing(
            @Param("flightId") String flightId,
            @Param("cabinClassCode") String cabinClassCode,
            @Param("newPrice") long newPrice,
            @Param("now") LocalDateTime now
    );
}
