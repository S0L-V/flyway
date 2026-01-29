package com.flyway.payment.dto;

import java.time.LocalDateTime;

import com.flyway.payment.domain.PaymentStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentDto {
    private String paymentId;
    private String reservationId;

    private String transactionId;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private Long amount;
    private String paymentMethod;
    private PaymentStatus status;
    private String orderId;
    private String paymentKey;
}
