package com.flyway.payment.domain;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequest {

    private String ReservationId;
    private Long amount;
    private String orderName; // 주문명 ( INC -> HND)
    private String customerName;
    private String customerEmail;

}
