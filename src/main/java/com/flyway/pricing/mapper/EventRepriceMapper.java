package com.flyway.pricing.mapper;

import com.flyway.pricing.model.EventRepriceSegment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EventRepriceMapper {
    List<EventRepriceSegment> selectRepriceSegments(@Param("reservationId") String reservationId);
}
