package com.flyway.pricing.dto;

import lombok.Getter;

@Getter
public class PricingResponse {

    /* ===== 계산 결과 ===== */
    private final long targetPrice;   // P_target
    private final long newPrice;      // P_new
    private final double r;
    private final double mLoad;
    private final double mTime;
    private final double alpha;

    /* ===== 상태 ===== */
    private final boolean applied;
    private final String skipReason;

    private PricingResponse(
            long targetPrice,
            long newPrice,
            double r,
            double mLoad,
            double mTime,
            double alpha,
            boolean applied,
            String skipReason
    ) {
        this.targetPrice = targetPrice;
        this.newPrice = newPrice;
        this.r = r;
        this.mLoad = mLoad;
        this.mTime = mTime;
        this.alpha = alpha;
        this.applied = applied;
        this.skipReason = skipReason;
    }

    /* ===== 성공 ===== */
    public static PricingResponse applied(
            long targetPrice,
            long newPrice,
            double r,
            double mLoad,
            double mTime,
            double alpha
    ) {
        return new PricingResponse(
                targetPrice,
                newPrice,
                r,
                mLoad,
                mTime,
                alpha,
                true,
                null
        );
    }

    /* ===== 스킵 ===== */
    public static PricingResponse skipped(String reason) {
        return new PricingResponse(
                0L,
                0L,
                0.0,
                0.0,
                0.0,
                0.0,
                false,
                reason
        );
    }
}