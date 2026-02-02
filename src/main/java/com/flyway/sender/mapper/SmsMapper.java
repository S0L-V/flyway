package com.flyway.sender.mapper;

import com.flyway.sender.dto.PassengerSmsInfo;
import com.flyway.sender.dto.PassengerTicketInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SmsMapper {

    String selectPhoneByUserId(@Param("userId") String userId);

    String selectPhoneByReservationId(@Param("reservationId") String reservationId);

    List<PassengerSmsInfo> selectPassengersByReservationId(
            @Param("reservationId") String reservationId);

    List<PassengerTicketInfo> selectPassengerTicketInfo(
            @Param("reservationId") String reservationId);
}
