package com.flyway.reservation.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BaggagePolicyView {

    private String policyId;
    private String cabinClassCode;      // ECO / BIZ / FST
    private String routeType;           // DOMESTIC / INTERNATIONAL

    private int freeCheckedBags;
    private int freeCheckedWeightKg;
    private Long extraBagFee;
    private Long overweightFeePerKg;
    private Integer maxWeightPerBagKg;
    private Integer maxTotalWeightKg;
}
