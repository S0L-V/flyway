package com.flyway.payment.domain;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentConfirmRequest {

    private String paymentKey;
    private String orderId;
    private Long amount;

}
