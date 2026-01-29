package com.flyway.payment.mapper;

import com.flyway.payment.dto.RefundSegmentDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface RefundMapper {

    void insertRefund(
            @Param("refundId") String refundId,
            @Param("reservationId") String reservationId,
            @Param("paymentId") String paymentId,
            @Param("rfId") String rfId,
            @Param("refundAmount") Long refundAmount,
            @Param("netRefundAmount") Long netRefundAmount,
            @Param("refundReason") String refundReason,
            @Param("requestedBy") String requestedBy
    );

    // 환불 시 segment 정보 조회
    List<RefundSegmentDto> selectSegmentsByReservationId(@Param("reservationId") String reservationId);

    // 환불 시 승객 수 조회
    int selectPassengerCountByReservationId(@Param("reservationId") String reservationId);

    // 환불 시 잔여석 복구
    void incrementSeat(@Param("flightId") String flightId,
                       @Param("cabinClass") String cabinClass,
                       @Param("count") int count);
}