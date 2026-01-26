package com.flyway.reservation.dto;

import lombok.*;

/**
 * 수하물 서비스 상세정보 (JSON 직렬화용)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BaggageServiceDetails {
    private int extraKg;
    private int extraBags;
    private long overweightFee;
    private long extraBagFee;
}