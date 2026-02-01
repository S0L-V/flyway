package com.flyway.pricing.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceHistoryInsert {
    private String priceHistoryId;
    private String flightId;
    private String cabinClassCode;

    private long currentPrice;
    private long oldPrice;

    private String updateType;      // EVENT
    private String triggerRefId;    // paymentId or refundId
    private String policyVersion;   // v1
    private String calcContextJson; // json
}
