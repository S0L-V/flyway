package com.flyway.pricing.batch.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flyway.pricing.service.DynamicPricingCalculator;
import com.flyway.pricing.batch.row.PricingResultRow;
import com.flyway.pricing.batch.row.RepriceCandidateRow;
import com.flyway.pricing.model.PricingInput;
import com.flyway.pricing.model.PricingResult;
import com.flyway.pricing.policy.PricingPolicy;
import com.flyway.pricing.service.SeatAvailabilityService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
@StepScope
public class RepriceProcessor implements ItemProcessor<RepriceCandidateRow, PricingResultRow> {

    private final DynamicPricingCalculator calculator;
    private final PricingPolicy pricingPolicy;
    private final ObjectMapper objectMapper; // JSON 변환기
    private final SeatAvailabilityService seatService;

    @Value("#{jobParameters['asOf']}") // JobParameter 주입
    private Long asOfEpochMillis;

    private static final ZoneId BATCH_ZONE = ZoneId.of("Asia/Seoul");

    @Override
    public PricingResultRow process(RepriceCandidateRow item) throws Exception {

        if (asOfEpochMillis == null) {
            throw new IllegalArgumentException("요구된 job parameter 'asOf' 가 누락되었습니다.");
        }

        // asOf 변환
        LocalDateTime batchReferenceTime =
                LocalDateTime.ofInstant(Instant.ofEpochMilli(asOfEpochMillis), BATCH_ZONE);

        // 1. DynamicPricingCalculator Input 생성
        PricingInput input = PricingInput.builder()
                .flightId(item.getFlightId())
                .cabinClassCode(item.getCabinClassCode())
                .basePrice(item.getBasePrice())
                .currentPrice(item.getCurrentPrice())
                .departureTime(item.getDepartureTime())
                .totalSeats(item.getTotalSeats())
                .remainingSeats(item.getRemainingSeats())
                .now(batchReferenceTime)
                .eventBased(false)
                .lastEventPricedAt(item.getLastEventPricedAt())
                .build();

        // 3. 계산 수행
        PricingResult result = calculator.calculate(input);

        // 4. 스킵 처리
        if (!result.isApplied()) {
            return null;
        }

        // 5. 계산 근거(ResultDto) JSON 생성
        CalcContext context = new CalcContext(
                result.getTargetPrice(),
                result.getR(),
                result.getMLoad(),
                result.getMTime(),
                result.getAlpha()
        );
        String contextJson = objectMapper.writeValueAsString(context);

        // 정책 버전
        String version = pricingPolicy.version();


        // 6. 결과 반환 (PricingResultRow DTO 생성)
        return PricingResultRow.builder()
                .flightId(item.getFlightId())
                .cabinClassCode(item.getCabinClassCode())
                .newPrice(result.getNewPrice())
                .currentPrice(item.getCurrentPrice())
                .applied(true)
                .policyVersion(version)
                //.calcContextJson(contextJson)
                .calculatedAt(batchReferenceTime)
                .build();
    }

    // 핵심 지표만 모아둔 DTO (calcContextJson로 db 저장)
    @Getter
    @AllArgsConstructor
    static class CalcContext {
        private long targetPrice; // 목표 가격 (스무딩 적용 전 이론가)
        private double r;         // 탑승률
        private double mLoad;     // 부하 계수
        private double mTime;     // 시간 계수
        private double alpha;     // 변동폭 조정 계수
    }
}
