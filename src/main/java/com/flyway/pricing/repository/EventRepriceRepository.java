package com.flyway.pricing.repository;

import com.flyway.pricing.model.EventRepriceSegment;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface EventRepriceRepository {
    List<EventRepriceSegment> findRepriceSegments(@Param("reservationId") String reservationId);
    int selectPassengerCount(@Param("reservationId") String reservationId);
}
