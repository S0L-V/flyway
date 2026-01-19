package com.flyway.pricing;

import com.flyway.pricing.dto.PricingRequest;
import com.flyway.pricing.dto.PricingResponse;
import com.flyway.pricing.policy.PricingPolicy;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 항공편 좌석 가격의 "동적 가격 산정"을 담당하는 도메인 서비스
 * DB/Batch/Transaction/Event와 분리된 로직
 *
 *  - 목표가: P_target = B * M_time(D/H) * M_load(r)
 *  - 스무딩: P_new = roundTo100(P_old + alpha(D/H) * (P_target - P_old))
 */
@Component
public class DynamicPricingCalculator {

    /* 이벤트 기반 가격 변동의 폭증 방지를 위한 쿨다운 시간 */
    private static final long EVENT_COOLDOWN_MINUTES = 10L;
    private static final long HOUR_MODE_THRESHOLD_HOURS = 48L;

    private final PricingPolicy policy;

    public DynamicPricingCalculator(PricingPolicy policy) {
        this.policy = policy;
    }

    /** 동적 가격 계산의 단일 진입점 */
    public PricingResponse calculate(PricingRequest req) {

        /* ==========================================================
         * 1. 입력값 기본 검증 (데이터 이상 방어)
         * ========================================================== */

        // 기본가가 0 이하거나 현재가 음수인 경우 → 계산 불가
        if (req.getBasePrice() <= 0 || req.getCurrentPrice() < 0) {
            return PricingResponse.skipped(PricingSkipReason.INVALID_PRICE);
        }

        // 좌석 데이터 정합성 검증
        if (req.getTotalSeats() <= 0 ||
                req.getRemainingSeats() < 0 ||
                req.getRemainingSeats() > req.getTotalSeats()) {
            return PricingResponse.skipped(PricingSkipReason.INVALID_SEATS);
        }

        // 시간 정보 누락 방어
        if (req.getDepartureTime() == null || req.getNow() == null) {
            return PricingResponse.skipped("INVALID_TIME");
        }

        /* ==========================================================
         * 2. 이벤트 기반 쿨다운(last_event_priced_at 고려)
         * ==========================================================
         */
        if (req.isEventBased() && req.getLastEventPricedAt() != null) {
            long minutes =
                    Duration.between(req.getLastEventPricedAt(), req.getNow()).toMinutes();

            // lastEventPricedAt이 미래인 이상치도 "쿨다운"으로 방어
            if (minutes < 0 || minutes < EVENT_COOLDOWN_MINUTES) {
                return PricingResponse.skipped(PricingSkipReason.COOLDOWN);
            }
        }

        /* ==========================================================
         * 3) 출발까지 남은 시간 계산
         * ==========================================================
         */
        long hoursToDeparture = Duration.between(req.getNow(), req.getDepartureTime()).toHours();
        long daysToDeparture = Duration.between(req.getNow(), req.getDepartureTime()).toDays();

        // (선택) Reader에서 걸러진다는 전제라도, 운영 안전장치로 남겨도 됨
        // if (hoursToDeparture < 0) return PricingResponse.skipped(PricingSkipReason.DEPARTED);

        boolean hourMode = hoursToDeparture <= HOUR_MODE_THRESHOLD_HOURS;

        /* ==========================================================
         * 4) 판매율 r 계산
         * r = soldSeats / totalSeats  (0~1 clamp)
         * ==========================================================
         */
        int soldSeats = req.getTotalSeats() - req.getRemainingSeats();
        double r = clamp01(soldSeats / (double) req.getTotalSeats());

        /* ==========================================================
         * 5) M_load(r) : 구간별 선형 보간
         * ========================================================== */
        double mLoad = policy.mLoad(r);

        /* ==========================================================
         * 6) M_time(D/H)
         * ========================================================== */
        double mTime = hourMode
                ? policy.mTimeHour(hoursToDeparture)
                : policy.mTimeDay(daysToDeparture);

        /* ==========================================================
         * 7) 목표가 P_target
         *    P_target = basePrice × M_time × M_load
         * ========================================================== */
        double targetRaw = req.getBasePrice() * mTime * mLoad;

        /* ==========================================================
         * 8) α 선택
         * - Day/Hour 전환
         * ========================================================== */
        double alpha = hourMode
                ? policy.alphaHour(hoursToDeparture)
                : policy.alphaDay(daysToDeparture);

        // 이벤트 기반 alpha 보정
        if (req.isEventBased()) {
            if (daysToDeparture > 2) alpha = Math.min(0.5, alpha);
            else alpha = 1.0;
        }

        /* ==========================================================
         * 9) 스무딩 적용 신규가
         *    P_new_raw = P_old + α × (P_target - P_old)
         * ========================================================== */
        double newPriceRaw = req.getCurrentPrice() + alpha * (targetRaw - req.getCurrentPrice());

        // 최종 반영 직전에만 100원 단위 반올림
        long newPrice = roundTo100(newPriceRaw);
        long targetPrice = roundTo100(targetRaw);

        /* ==========================================================
         * 10) 미세 변동 스킵 (100원 미만)
         * ========================================================== */
        if (newPrice == req.getCurrentPrice()) {
            return PricingResponse.skipped(PricingSkipReason.SMALL_DIFF);
        }

        return PricingResponse.applied(targetPrice, newPrice, r, mLoad, mTime, alpha);
    }

    // ------------------------------------------------------------
    // Util
    // ------------------------------------------------------------

    /** r을 0~1 범위로 클램프 */
    private static double clamp01(double r) {
        return Math.max(0.0, Math.min(1.0, r));
    }

    /** 최종 반영 시점에만 100원 단위 반올림 */
    private static long roundTo100(double price) {
        return Math.round(price / 100.0) * 100L;
    }
}
