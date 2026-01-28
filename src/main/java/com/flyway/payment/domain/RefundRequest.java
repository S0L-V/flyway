package com.flyway.payment.domain;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundRequest {

    private String paymentKey;
    private String cancelReason;
    private Long cancelAmount;

}
