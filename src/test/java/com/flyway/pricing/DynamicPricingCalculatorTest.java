package com.flyway.pricing;

import com.flyway.pricing.dto.PricingRequest;
import com.flyway.pricing.dto.PricingResponse;
import com.flyway.pricing.policy.PricingPolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DynamicPricingCalculatorTest {

    /**
     * 테스트는 특정 구현체(PricingPolicyV1)에 의존하지 않도록,
     * 스펙 기준의 정책을 테스트 더블로 주입한다.
     */
    private static final PricingPolicy POLICY = new SpecPolicyV1();
    private final DynamicPricingCalculator calculator = new DynamicPricingCalculator(POLICY);

    /**
     * 정책 스펙 기반 테스트 더블
     * - mLoad: 구간별 선형 보간(첨부 스펙)
     * - alphaDay/alphaHour: 설계안
     * - mTimeDay: 첨부 표(0.95/1.00/1.10/1.25/1.50)
     * - mTimeHour: 프로젝트에서 별도 튜닝 가능하므로, 여기서는 '경계 불연속 방지' 목적의 기본값만 둔다.
     */
    private static class SpecPolicyV1 implements PricingPolicy {
        @Override
        public String version() {
            return "v1.0";
        }

        @Override
        public double mLoad(double r) {
            if (r < 0.40) return 0.95 + 0.125 * r;
            if (r < 0.60) return 1.00 + 0.75 * (r - 0.40);
            if (r < 0.80) return 1.15 + 1.00 * (r - 0.60);
            return 1.35 + 0.50 * (r - 0.80);
        }

        @Override
        public double alphaDay(long d) {
            if (d >= 45) return 0.10;
            if (d >= 30) return 0.15;
            if (d >= 14) return 0.20;
            if (d >= 7)  return 0.30;
            if (d >= 2)  return 0.40;
            return 0.40;
        }

        @Override
        public double alphaHour(long h) {
            if (h > 24) return 0.50;
            if (h > 6)  return 0.70;
            return 1.00;
        }

        @Override
        public double mTimeDay(long d) {
            if (d >= 30) return 0.95;
            if (d >= 21) return 1.00;
            if (d >= 14) return 1.10;
            if (d >= 7)  return 1.25;
            return 1.50; // 0<=D<7
        }

        @Override
        public double mTimeHour(long h) {
            // 48h 경계에서 Day(D=2) 구간의 1.50과 맞춰 불연속을 방지하는 기본안
            if (h > 24) return 1.50;
            if (h > 6)  return 1.70;
            return 1.90;
        }
    }

    // ====== 공통 기본값 ======
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 19, 12, 0);

    // 기본 요청 생성기 (테스트에서 필요한 값만 덮어쓰기)
    private PricingRequest.PricingRequestBuilder baseReq() {
        return PricingRequest.builder()
                .flightId("FL-1")
                .cabinClassCode("ECO")
                .basePrice(100_000L)
                .currentPrice(90_000L)
                .remainingSeats(60)
                .totalSeats(100)
                .departureTime(NOW.plusDays(30))
                .now(NOW)
                .eventBased(false)
                .lastEventPricedAt(null);
    }

    // ------------------------------------------------------------
    // 1) 입력 검증 테스트
    // ------------------------------------------------------------

    @Test
    @DisplayName("basePrice <= 0 이면 INVALID_PRICE 스킵")
    void invalidPrice_basePriceZero() {
        PricingRequest req = baseReq()
                .basePrice(0)
                .build();

        PricingResponse res = calculator.calculate(req);

        assertFalse(res.isApplied());
        assertEquals(PricingSkipReason.INVALID_PRICE, res.getSkipReason());
    }

    @Test
    @DisplayName("currentPrice < 0 이면 INVALID_PRICE 스킵")
    void invalidPrice_currentPriceNegative() {
        PricingRequest req = baseReq()
                .currentPrice(-1)
                .build();

        PricingResponse res = calculator.calculate(req);

        assertFalse(res.isApplied());
        assertEquals(PricingSkipReason.INVALID_PRICE, res.getSkipReason());
    }

    @Test
    @DisplayName("totalSeats <= 0 이면 INVALID_SEATS 스킵")
    void invalidSeats_totalZero() {
        PricingRequest req = baseReq()
                .totalSeats(0)
                .build();

        PricingResponse res = calculator.calculate(req);

        assertFalse(res.isApplied());
        assertEquals(PricingSkipReason.INVALID_SEATS, res.getSkipReason());
    }

    @Test
    @DisplayName("remainingSeats > totalSeats 이면 INVALID_SEATS 스킵")
    void invalidSeats_remainingGreaterThanTotal() {
        PricingRequest req = baseReq()
                .remainingSeats(101)
                .totalSeats(100)
                .build();

        PricingResponse res = calculator.calculate(req);

        assertFalse(res.isApplied());
        assertEquals(PricingSkipReason.INVALID_SEATS, res.getSkipReason());
    }

    @Test
    @DisplayName("departureTime/now 누락이면 INVALID_TIME 스킵")
    void invalidTime_null() {
        PricingRequest req = baseReq()
                .departureTime(null)
                .build();

        PricingResponse res = calculator.calculate(req);

        assertFalse(res.isApplied());
        assertEquals(PricingSkipReason.INVALID_TIME, res.getSkipReason());
    }

    // ------------------------------------------------------------
    // 2) 이벤트 쿨다운 테스트 (eventBased=true일 때만)
    // ------------------------------------------------------------

    @Test
    @DisplayName("이벤트 기반: last_event_priced_at이 10분 이내면 COOLDOWN 스킵")
    void cooldown_eventOnly_shouldSkip() {
        PricingRequest req = baseReq()
                .eventBased(true)
                .lastEventPricedAt(NOW.minusMinutes(9)) // 10분 이내
                .build();

        PricingResponse res = calculator.calculate(req);

        assertFalse(res.isApplied());
        assertEquals(PricingSkipReason.COOLDOWN, res.getSkipReason());
    }

    @Test
    @DisplayName("배치 기반: last_event_priced_at이 있어도 쿨다운 미적용")
    void cooldown_batchShouldNotApply() {
        PricingRequest req = baseReq()
                .eventBased(false)
                .lastEventPricedAt(NOW.minusMinutes(1)) // 있어도 무시
                // 가격 변화를 강제로 만들기 위해 r을 높여 target을 올림
                .remainingSeats(0)   // sold=100, r=1.0 -> M_load=1.45
                .currentPrice(100_000L)
                .build();

        PricingResponse res = calculator.calculate(req);

        // 배치에서는 쿨다운 스킵이 아니라, 적용 또는 SMALL_DIFF여야 함.
        // (정책 v1.0) D=30이면 mTimeDay=0.95
        // r=1.0이면 mLoad=1.45
        // targetRaw = 100,000 * 0.95 * 1.45 = 137,750 -> 137,800
        // newRaw    = 100,000 + 0.15*(137,750-100,000) = 105,662.5 -> 105,700
        assertTrue(res.isApplied());
        assertEquals(105_700L, res.getNewPrice());
        assertNull(res.getSkipReason());
    }

    // ------------------------------------------------------------
    // 3) M_load(r) 구간 검증 (결과값의 방향성과 특정 케이스)
    // ------------------------------------------------------------

    @ParameterizedTest(name = "r={0} -> M_load 예상={1}")
    @CsvSource({
            // r=0.0 -> 0.95
            "0.00, 0.95",
            // r=0.40 -> 1.00
            "0.40, 1.00",
            // r=0.60 -> 1.15
            "0.60, 1.15",
            // r=0.80 -> 1.35
            "0.80, 1.35",
            // r=1.00 -> 1.45
            "1.00, 1.45"
    })
    @DisplayName("M_load(r) 기준점 값 검증 (구간 경계)")
    void mLoad_boundaries(double r, double expectedMLoad) {
        // r를 만들기 위해 total=100 고정, sold = r*100
        int total = 100;
        int sold = (int) Math.round(r * total);
        int remaining = total - sold;

        PricingRequest req = baseReq()
                .basePrice(100_000L)
                // 경계점에서도 SMALL_DIFF로 스킵되지 않도록 현재가를 충분히 낮게 설정
                .currentPrice(10_000L)
                .totalSeats(total)
                .remainingSeats(remaining)
                // 변화를 강제로 만들기 위해 D를 크게(배치), alphaDay가 작아도 diff가 100 이상 나게 구성
                .departureTime(NOW.plusDays(30))
                .build();

        PricingResponse res = calculator.calculate(req);

        // 현재가를 낮게 설정했으므로 변화가 발생하여 applied 기대
        assertTrue(res.isApplied());
        assertEquals(expectedMLoad, res.getMLoad(), 1e-9);
    }

    // ------------------------------------------------------------
    // 4) alpha 규칙 검증 (배치/이벤트, Day/Hour)
    // ------------------------------------------------------------

    @Test
    @DisplayName("배치 + Day 기반: D=30 -> alpha=0.15")
    void alpha_batch_day_30() {
        PricingRequest req = baseReq()
                .eventBased(false)
                .departureTime(NOW.plusDays(30)) // hours>48 -> day
                // 변화 유도: r=1.0
                .remainingSeats(0)
                .build();

        PricingResponse res = calculator.calculate(req);

        assertTrue(res.isApplied());
        assertEquals(0.15, res.getAlpha(), 1e-9);
    }

    @Test
    @DisplayName("배치 + Hour 기반: 24<h<=48 구간이면 alpha=0.50")
    void alpha_batch_hour_48_to_24() {
        PricingRequest req = baseReq()
                .eventBased(false)
                .departureTime(NOW.plusHours(30)) // hour 기반
                .remainingSeats(0)                // r=1.0
                .build();

        PricingResponse res = calculator.calculate(req);

        assertTrue(res.isApplied());
        assertEquals(0.50, res.getAlpha(), 1e-9);
    }

    @Test
    @DisplayName("이벤트 + D>2: alpha = min(0.5, baseAlpha)")
    void alpha_event_d_gt_2_min_05() {
        // D=30이면 baseAlpha=0.15 -> min(0.5,0.15)=0.15
        PricingRequest req = baseReq()
                .eventBased(true)
                .lastEventPricedAt(NOW.minusMinutes(20)) // 쿨다운 통과
                .departureTime(NOW.plusDays(30))
                .remainingSeats(0) // r=1.0
                .build();

        PricingResponse res = calculator.calculate(req);

        assertTrue(res.isApplied());
        assertEquals(0.15, res.getAlpha(), 1e-9);
    }

    @Test
    @DisplayName("이벤트 + D<=2: alpha=1.0 (즉시 반영)")
    void alpha_event_d_le_2_immediate() {
        PricingRequest req = baseReq()
                .eventBased(true)
                .lastEventPricedAt(NOW.minusMinutes(20))
                .departureTime(NOW.plusHours(30)) // 대략 D<=2
                .remainingSeats(0) // r=1.0
                // 현재가를 낮춰 즉시 반영 효과가 보이게
                .currentPrice(100_000L)
                .build();

        PricingResponse res = calculator.calculate(req);

        assertTrue(res.isApplied());
        assertEquals(1.0, res.getAlpha(), 1e-9);

        // 즉시 반영이면 newPrice ≈ targetPrice(반올림 영향만)
        assertEquals(res.getTargetPrice(), res.getNewPrice());
    }

    // ------------------------------------------------------------
    // 5) 100원 미만 변동 스킵 검증
    // ------------------------------------------------------------

    @Test
    @DisplayName("반올림 후 newPrice == currentPrice 이면 SMALL_DIFF 스킵")
    void smallDiff_shouldSkip() {
        // (정책 v1.0) D=30이면 mTimeDay=0.95
        // r=0.0이면 mLoad=0.95 -> targetRaw=100,000*0.95*0.95=90,250
        // currentPrice=90,300이면 newRaw=90,300 + 0.15*(90,250-90,300)=90,292.5 -> 90,300
        PricingRequest req = baseReq()
                .basePrice(100_000L)
                .currentPrice(90_300L)
                // r=0.0 만들기: sold=0 => remaining=total
                .totalSeats(100)
                .remainingSeats(100)
                .build();

        PricingResponse res = calculator.calculate(req);

        assertFalse(res.isApplied());
        assertEquals(PricingSkipReason.SMALL_DIFF, res.getSkipReason());
    }

    // ------------------------------------------------------------
    // 6) 100원 단위 반올림 검증 (대표 케이스)
    // ------------------------------------------------------------

    @Test
    @DisplayName("반올림: newRaw=105,662.5이면 105,700으로 반올림된다")
    void rounding_to_100() {
        // (정책 v1.0)
        // r=1.0 => mLoad=1.45, D=30 => mTimeDay=0.95 => targetRaw=137,750 -> 137,800
        // alphaDay(D=30)=0.15
        // newRaw = 100,000 + 0.15*(137,750-100,000)=105,662.5 -> 105,700
        PricingRequest req = baseReq()
                .eventBased(false)
                .basePrice(100_000L)
                .currentPrice(100_000L)
                .totalSeats(100)
                .remainingSeats(0)
                .departureTime(NOW.plusDays(30))
                .build();

        PricingResponse res = calculator.calculate(req);

        assertTrue(res.isApplied());
        assertEquals(137_800L, res.getTargetPrice());
        assertEquals(105_700L, res.getNewPrice());
    }




    // ------------------------------------------------------------
    // 7) 경계값 테스트 (잔여 0, 출발 임박, alpha 경계, r 경계 주변)
    // ------------------------------------------------------------

    @Test
    @DisplayName("경계값(잔여 0): remainingSeats=0이면 r=1.0, M_load=1.45")
    void boundary_remainingZero_rIsOne() {
        PricingRequest req = baseReq()
                .eventBased(false)
                .totalSeats(100)
                .remainingSeats(0)              // sold=100 => r=1.0
                .departureTime(NOW.plusDays(30)) // day 기반
                .build();

        PricingResponse res = calculator.calculate(req);

        assertTrue(res.isApplied());
        assertEquals(1.0, res.getR(), 1e-12);
        assertEquals(1.45, res.getMLoad(), 1e-12);
    }

    @Test
    @DisplayName("경계값(출발 임박): 이벤트 + H<=6이면 alpha=1.0, newPrice==targetPrice")
    void boundary_departureImminent_event_alphaImmediate() {
        PricingRequest req = baseReq()
                .eventBased(true)
                .lastEventPricedAt(NOW.minusMinutes(20)) // 쿨다운 통과
                .departureTime(NOW.plusHours(6))         // H=6 경계 (<=6 -> 1.0)
                .totalSeats(100)
                .remainingSeats(0)                       // r=1.0 -> target 상승
                .currentPrice(100_000L)
                .build();

        PricingResponse res = calculator.calculate(req);

        assertTrue(res.isApplied());
        assertEquals(1.0, res.getAlpha(), 1e-12);
        assertEquals(res.getTargetPrice(), res.getNewPrice());
    }

    @Test
    @DisplayName("alphaHour 경계: H=7이면 0.70, H=6이면 1.00")
    void boundary_alphaHour_7_vs_6() {
        // H=7 -> alpha=0.70
        PricingRequest req7 = baseReq()
                .eventBased(false)
                .departureTime(NOW.plusHours(7))
                .totalSeats(100)
                .remainingSeats(0) // r=1.0 -> 변화 유도
                .build();
        PricingResponse res7 = calculator.calculate(req7);
        assertTrue(res7.isApplied());
        assertEquals(0.70, res7.getAlpha(), 1e-12);

        // H=6 -> alpha=1.00
        PricingRequest req6 = baseReq()
                .eventBased(false)
                .departureTime(NOW.plusHours(6))
                .totalSeats(100)
                .remainingSeats(0)
                .build();
        PricingResponse res6 = calculator.calculate(req6);
        assertTrue(res6.isApplied());
        assertEquals(1.00, res6.getAlpha(), 1e-12);
    }

    @Test
    @DisplayName("alphaHour 경계: H=25이면 0.50, H=24이면 0.70")
    void boundary_alphaHour_25_vs_24() {
        PricingRequest req25 = baseReq()
                .eventBased(false)
                .departureTime(NOW.plusHours(25)) // >24 => 0.50
                .totalSeats(100)
                .remainingSeats(0)
                .build();
        PricingResponse res25 = calculator.calculate(req25);
        assertTrue(res25.isApplied());
        assertEquals(0.50, res25.getAlpha(), 1e-12);

        PricingRequest req24 = baseReq()
                .eventBased(false)
                .departureTime(NOW.plusHours(24)) // <=24 && >6 => 0.70
                .totalSeats(100)
                .remainingSeats(0)
                .build();
        PricingResponse res24 = calculator.calculate(req24);
        assertTrue(res24.isApplied());
        assertEquals(0.70, res24.getAlpha(), 1e-12);
    }

    @Test
    @DisplayName("Day/Hour 전환 경계: H=48은 Hour(0.50), H=49는 Day(D=2~)로 계산")
    void boundary_dayHourSwitch_48_vs_49() {
        // H=48 => hour 기반 => alphaHour(48)=0.50
        PricingRequest req48 = baseReq()
                .eventBased(false)
                .departureTime(NOW.plusHours(48))
                .totalSeats(100)
                .remainingSeats(0)
                .build();
        PricingResponse res48 = calculator.calculate(req48);
        assertTrue(res48.isApplied());
        assertEquals(0.50, res48.getAlpha(), 1e-12);

        // H=49 => day 기반으로 전환 (Duration.toDays()는 49h -> 2days)
        PricingRequest req49 = baseReq()
                .eventBased(false)
                .departureTime(NOW.plusHours(49))
                .totalSeats(100)
                .remainingSeats(0)
                .build();
        PricingResponse res49 = calculator.calculate(req49);
        assertTrue(res49.isApplied());
        // 49h => daysToDeparture=2 -> alphaDay(2)=0.40
        assertEquals(0.40, res49.getAlpha(), 1e-12);
    }

    @Test
    @DisplayName("r 경계 주변(0.399 vs 0.401): M_load가 단조 증가하고 경계 근처에서 연속적으로 변화")
    void boundary_r_near_040() {
        // total=1000으로 해서 r을 0.399/0.401 정밀하게 만듦
        int total = 1000;

        // r=0.399 -> sold=399 -> remaining=601
        PricingRequest reqLow = baseReq()
                .totalSeats(total)
                .remainingSeats(601)
                .departureTime(NOW.plusDays(30))
                .build();
        PricingResponse low = calculator.calculate(reqLow);
        assertTrue(low.isApplied());

        // r=0.401 -> sold=401 -> remaining=599
        PricingRequest reqHigh = baseReq()
                .totalSeats(total)
                .remainingSeats(599)
                .departureTime(NOW.plusDays(30))
                .build();
        PricingResponse high = calculator.calculate(reqHigh);
        assertTrue(high.isApplied());

        assertTrue(high.getMLoad() > low.getMLoad(), "r 증가 시 M_load도 증가해야 함");
        // 경계 근처에서 차이가 과도하게 튀지 않는지(연속성 간접 검증)
        assertTrue(Math.abs(high.getMLoad() - low.getMLoad()) < 0.05, "경계 근처 변화는 급격하지 않아야 함");
    }

    // ------------------------------------------------------------
    // 8) 예외/이상치 입력 테스트 (쿨다운 경계, 미래 lastEvent, clamp 등)
    // ------------------------------------------------------------

    @Test
    @DisplayName("이벤트 쿨다운 경계: 정확히 10분이면 COOLDOWN이 아니다(통과)")
    void anomaly_cooldown_exactly10min_shouldPass() {
        PricingRequest req = baseReq()
                .eventBased(true)
                .lastEventPricedAt(NOW.minusMinutes(10)) // 경계: 10분
                .totalSeats(100)
                .remainingSeats(0) // 변화 유도
                .build();

        PricingResponse res = calculator.calculate(req);

        // 통과하면 applied 또는 SMALL_DIFF 여야 하는데, 여기선 변화가 커서 applied 기대
        assertTrue(res.isApplied());
        assertNull(res.getSkipReason());
    }

    @Test
    @DisplayName("이벤트 쿨다운 이상치: lastEventPricedAt이 미래면 COOLDOWN 스킵(데이터 이상 방어)")
    void anomaly_lastEventInFuture_shouldCooldownSkip() {
        PricingRequest req = baseReq()
                .eventBased(true)
                .lastEventPricedAt(NOW.plusMinutes(1)) // 미래 시각
                .totalSeats(100)
                .remainingSeats(0)
                .build();

        PricingResponse res = calculator.calculate(req);

        assertFalse(res.isApplied());
        assertEquals(PricingSkipReason.COOLDOWN, res.getSkipReason());
    }

    @Test
    @DisplayName("r clamp 이상치: remainingSeats가 0 미만이면 INVALID_SEATS로 스킵 (clamp로 숨기지 않음)")
    void anomaly_remainingNegative_shouldInvalidSeats() {
        PricingRequest req = baseReq()
                .totalSeats(100)
                .remainingSeats(-1)
                .build();

        PricingResponse res = calculator.calculate(req);

        assertFalse(res.isApplied());
        assertEquals(PricingSkipReason.INVALID_SEATS, res.getSkipReason());
    }

    @Test
    @DisplayName("극단값: 매우 큰 basePrice에서도 오버플로 없이 100원 반올림 결과가 정상")
    void anomaly_largeBasePrice_roundingStable() {
        // long 범위 내 큰 값 (곱셈은 double로 가므로 overflow는 double 정밀도 이슈만 주의)
        PricingRequest req = baseReq()
                .basePrice(9_000_000_000_000L) // 9조
                .currentPrice(9_000_000_000_000L)
                .totalSeats(100)
                .remainingSeats(0) // r=1.0 -> target=1.45배
                .departureTime(NOW.plusDays(45)) // alphaDay=0.10
                .build();

        PricingResponse res = calculator.calculate(req);

        assertTrue(res.isApplied());
        // 최소한 100원 단위로 끝나는지(반올림 적용) 확인
        assertEquals(0L, res.getNewPrice() % 100L);
        assertEquals(0L, res.getTargetPrice() % 100L);
    }

    // ------------------------------------------------------------
    // 9) 동일 입력 → 동일 출력 (Idempotent / Deterministic)
    // ------------------------------------------------------------

    @Test
    @DisplayName("동일 입력 2회 호출 시 결과가 동일해야 한다 (idempotent)")
    void idempotent_sameInput_sameOutput() {
        PricingRequest req = baseReq()
                .eventBased(true)
                .lastEventPricedAt(NOW.minusMinutes(20))
                .departureTime(NOW.plusDays(30))
                .totalSeats(100)
                .remainingSeats(0)   // r=1.0
                .basePrice(100_000L)
                .currentPrice(100_000L)
                .build();

        PricingResponse r1 = calculator.calculate(req);
        PricingResponse r2 = calculator.calculate(req);

        // PricingResult가 equals를 구현하지 않아도 필드 비교로 결정성 검증 가능
        assertEquals(r1.isApplied(), r2.isApplied());
        assertEquals(r1.getSkipReason(), r2.getSkipReason());
        assertEquals(r1.getTargetPrice(), r2.getTargetPrice());
        assertEquals(r1.getNewPrice(), r2.getNewPrice());
        assertEquals(r1.getR(), r2.getR(), 1e-12);
        assertEquals(r1.getMLoad(), r2.getMLoad(), 1e-12);
        assertEquals(r1.getMTime(), r2.getMTime(), 1e-12);
        assertEquals(r1.getAlpha(), r2.getAlpha(), 1e-12);
    }

    @Test
    @DisplayName("idempotent(스킵 케이스): 동일 입력이면 동일 스킵 사유를 반환해야 한다")
    void idempotent_skipCase_sameReason() {
        // SMALL_DIFF 유도:
        // r=0.4 -> mLoad=1.0
        // D=25 -> mTimeDay=1.0(21<=D<30)
        // base=current -> target==current -> new==current -> SMALL_DIFF
        PricingRequest req = baseReq()
                .basePrice(100_000L)
                .currentPrice(100_000L)
                .totalSeats(100)
                .remainingSeats(60) // sold=40 -> r=0.4 -> mLoad=1.0
                .eventBased(false)
                .departureTime(NOW.plusDays(25)) // mTimeDay=1.0 구간으로 이동
                .build();

        PricingResponse r1 = calculator.calculate(req);
        PricingResponse r2 = calculator.calculate(req);

        assertFalse(r1.isApplied());
        assertFalse(r2.isApplied());
        assertEquals(PricingSkipReason.SMALL_DIFF, r1.getSkipReason());
        assertEquals(r1.getSkipReason(), r2.getSkipReason());
    }

}