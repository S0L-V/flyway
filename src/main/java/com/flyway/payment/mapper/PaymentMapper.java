package com.flyway.payment.mapper;

import com.flyway.payment.dto.PaymentViewDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PaymentMapper {

    // 결제 정보 버장
    int insertPayment(PaymentViewDto dto);

    // 결제 id, 결제정보 return
    PaymentViewDto selectByPaymentId(@Param("paymentId") String paymentId);

    //주문 id로 조회
    PaymentViewDto selectByOrderId(@Param("orderId") String orderId);

    //예약 id로 조회
    PaymentViewDto selectByReservationId(@Param("reservationId") String reservationId);

    int updateStatus(@Param("paymentId") String paymentId,
                     @Param("status") String status);

    int updatePaymentKey(@Param("paymentId") String paymentId,
                         @Param("paymentKey") String paymentKey);

    int updatePaymentComplete(@Param("paymentId") String paymentId,
                              @Param("paymentKey") String paymentKey,
                              @Param("status") String status,
                              @Param("method") String method);
    List<PaymentViewDto> selectByUserId(@Param("userId") String userId);

}
