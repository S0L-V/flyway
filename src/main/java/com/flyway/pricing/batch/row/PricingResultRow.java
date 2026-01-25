package com.flyway.pricing.batch.row;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class PricingResultRow {
    private String flightId;
    private String cabinClassCode;

    private long newPrice;     // DB: current_price (새로운 가격)
    private long currentPrice; // DB: old_price (기존 가격)

    private boolean applied;
    private LocalDateTime calculatedAt;

    private String policyVersion; // 정책 버전 ("v1")
    private String calcContextJson; // 계산 근거 JSON
}