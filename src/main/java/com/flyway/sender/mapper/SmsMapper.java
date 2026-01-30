package com.flyway.sender.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SmsMapper {

    String selectPhoneByUserId(@Param("userId") String userId);

    String selectPhoneByReservationId(@Param("reservationId") String reservationId);
}
