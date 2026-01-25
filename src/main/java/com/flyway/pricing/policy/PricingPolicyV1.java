package com.flyway.pricing.policy;

import org.springframework.stereotype.Component;

@Component
public final class PricingPolicyV1 implements PricingPolicy {
    @Override
    public String version() {
        return "v1";
    }

    /** M_load(r) : 구간별 선형 보간 */
    @Override
    public double mLoad(double r) {
        if (r < 0.40) return 0.95 + 0.125 * r;
        if (r < 0.60) return 1.00 + 0.75 * (r - 0.40);
        if (r < 0.80) return 1.15 + 1.00 * (r - 0.60);
        return 1.35 + 0.50 * (r - 0.80);
    }

    /**
     * M_time(D) 정책
     * D-30 ~ D-2를 대상으로 적용
     */
    @Override
    public double mTimeDay(long d) {
        if (d >= 30) return 0.95;
        if (d >= 21) return 1.00;
        if (d >= 14) return 1.10;
        if (d >= 7) return 1.25;
        // 0 <= D < 7
        return 1.50;
    }

    /**
     * M_time(H) 정책
     * H-48 ~ H-0 대상으로 적용
     */
    @Override
    public double mTimeHour(long h) {
        if (h > 24) return 1.50; // 48>=h>24 : Day(D<7)의 1.50과 연속
        if (h > 12) return 1.60; // 24>=h>12
        if (h > 6) return 1.70; // 12>=h>6
        if (h > 1) return 1.80; // 6>=h>1
        return 1.90;             // 1>=h>=0 : 출발 직전 프리미엄
    }

    /**
     * αlpha 정책
     * - 기본: 48시간 이내면 Hour 기반, 그 외 Day 기반
     * - 이벤트 기반:
     * - D > 2: α = min(0.5, α(D/H))
     * - D <= 2: α = 1.0 (즉시 반영)
     */
    @Override
    public double alphaDay(long d) {
        if (d >= 45) return 0.10;
        if (d >= 30) return 0.15;
        if (d >= 14) return 0.20;
        if (d >= 7) return 0.30;
        if (d >= 2) return 0.40;
        return 0.40;
    }

    @Override
    public double alphaHour(long h) {
        if (h > 24) return 0.50; // 48 >= h > 24
        if (h > 6) return 0.70; // 24 >= h > 6
        return 1.00;             // h <= 6
    }
}


