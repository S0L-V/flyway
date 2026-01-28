package com.flyway.payment.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentViewDto {

    private String paymentId;        // 우리 DB의 결제 ID
    private String reservationId;    // 예약 ID
    private String paymentKey;       // 토스 결제 키
    private String orderId;          // 주문 ID
    private Long amount;             // 결제 금액
    private String method;           // 결제 수단
    private String status;           // 결제 상태 (PAID, CANCELLED, REFUNDED)
    private LocalDateTime paidAt;    // 결제 완료 시각
    private LocalDateTime createdAt; // 생성 시각
}
