package com.flyway.reservation.mapper;

import com.flyway.reservation.dto.ReservationSummaryDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReservationQueryMapper {

    List<ReservationSummaryDto> findReservationHistoriesByUserId(
            @Param("userId") String userId,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    int countReservationHistoriesByUserId(
            @Param("userId") String userId
    );
}
