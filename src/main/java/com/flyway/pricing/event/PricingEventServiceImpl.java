package com.flyway.pricing.event;

import com.flyway.pricing.model.EventRepriceSegment;
import com.flyway.pricing.model.FlightSeatPriceRow;
import com.flyway.pricing.model.PricingInput;
import com.flyway.pricing.model.PricingResult;
import com.flyway.pricing.policy.PricingPolicy;
import com.flyway.pricing.repository.EventRepriceRepository;
import com.flyway.pricing.repository.FlightSeatPriceRepository;
import com.flyway.pricing.repository.PriceHistoryRepository;
import com.flyway.pricing.service.DynamicPricingCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 결제/환불 등 "이벤트" 발생 시점에 수행하는 재가격 오케스트레이션 서비스.
 *
 * - PaymentService.completePayment()에서 좌석 BOOKED 반영 이후 호출
 * - reservationId -> segments 조회
 * - (flightId, cabinClassCode)별로 flight_seat_price 조회 후 calculator 호출
 * - PricingResult.applied 인 경우에만 DB 반영 + price_history insert
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PricingEventServiceImpl implements PricingEventService {

    private final DynamicPricingCalculator pricingCalculator;
    private final PricingPolicy pricingPolicy;

    private final FlightSeatPriceRepository flightSeatPriceRepository;
    private final EventRepriceRepository eventRepriceRepository;
    private final PriceHistoryRepository priceHistoryRepository;

    /**
     * 결제 완료(좌석 확정) 이후 재가격 수행
     */
    @Override
    @Transactional
    public RepriceSummary repriceAfterPayment(String reservationId, String paymentId) {
        return repriceByReservation(reservationId, paymentId, EventType.PAYMENT);
    }

    /**
     * 환불 이후 재가격 수행
     */
    @Override
    @Transactional
    public RepriceSummary repriceAfterRefund(String reservationId, String refundId) {
        return repriceByReservation(reservationId, refundId, EventType.REFUND);
    }

    private RepriceSummary repriceByReservation(String reservationId, String triggerId, EventType eventType) {
        LocalDateTime now = LocalDateTime.now();

        // 예약 구간 조회
        List<EventRepriceSegment> segments = eventRepriceRepository.findRepriceSegments(reservationId);

        if (segments.isEmpty()) {
            log.warn("[EVENT_REPRICE] no segments. reservationId={}", reservationId);
            return new RepriceSummary(reservationId, triggerId, eventType, 0, 0, 0,
                    java.time.Instant.now(), List.of());
        }

        // 데드락 방지: 락 획득 순서를 항상 동일하게
        segments.sort(
                Comparator.comparing(EventRepriceSegment::getFlightId)
                        .thenComparing(EventRepriceSegment::getCabinClassCode)
        );

        // 승객 수 (좌석 변화량 기록용)
        int passengerCount = eventRepriceRepository.selectPassengerCount(reservationId);


        // 중복 방지: flightId|cabin
        Set<String> dedup = new HashSet<>();

        int applied = 0;
        int skipped = 0;
        List<RepriceItemResult> items = new ArrayList<>();

        for (var seg : segments) {
            String flightId = seg.getFlightId();
            String cabin = seg.getCabinClassCode();

            if (!dedup.add(flightId + "|" + cabin)) continue;

            boolean bypass = (eventType == EventType.REFUND);

            RepriceItemResult item = repriceOne(flightId, cabin, triggerId, passengerCount, now, bypass);
            items.add(item);

            if (item.newPrice() != null) applied++;
            else skipped++;
        }

        return new RepriceSummary(
                reservationId,
                triggerId,
                eventType,
                dedup.size(),
                applied,
                skipped,
                java.time.Instant.now(),
                items
        );
    }

    private RepriceItemResult repriceOne(
            String flightId,
            String cabinClassCode,
            String triggerRefId,
            int passengerCount,
            LocalDateTime now,
            Boolean bypass
    ) {
        FlightSeatPriceRow row = flightSeatPriceRepository.lockForUpdate(flightId, cabinClassCode);

        if (row == null) {
            return new RepriceItemResult(flightId, cabinClassCode, null, null, "NO_ROW");
        }

        PricingInput input = PricingInput.builder()
                .flightId(flightId)
                .cabinClassCode(cabinClassCode)
                .basePrice(row.getBasePrice())
                .currentPrice(row.getCurrentPrice())
                .totalSeats(row.getTotalSeats())
                .remainingSeats(row.getRemainingSeats())
                .departureTime(row.getDepartureTime())
                .now(now)
                .eventBased(true)
                .bypassCooldown(bypass)
                .lastEventPricedAt(row.getLastEventPricedAt())
                .build();

        PricingResult result = pricingCalculator.calculate(input);

        if (!result.isApplied()) {
            return new RepriceItemResult(flightId, cabinClassCode,
                    row.getCurrentPrice(), null, result.getSkipReason());
        }

        // 가격 반영
        flightSeatPriceRepository.updateAfterEventPricing(
                flightId, cabinClassCode, result.getNewPrice(), now
        );

        // 이력 적재
        priceHistoryRepository.insertEventHistory(
                com.flyway.pricing.model.PriceHistoryInsert.builder()
                        .priceHistoryId(UUID.randomUUID().toString())
                        .flightId(flightId)
                        .cabinClassCode(cabinClassCode)
                        .currentPrice(result.getNewPrice())
                        .oldPrice(row.getCurrentPrice())
                        .updateType("EVENT")
                        .triggerRefId(triggerRefId)
                        .policyVersion(pricingPolicy.version())
                        .calcContextJson(buildCalcContextJson(result, passengerCount))
                        .build()
        );

        return new RepriceItemResult(flightId, cabinClassCode,
                row.getCurrentPrice(), result.getNewPrice(), "APPLIED");
    }

    private String buildCalcContextJson(PricingResult result, int passengerCount) {
        return String.format(
                Locale.US,
                "{\"r\":%.4f,\"alpha\":%.4f,\"target\":%d,\"passengers\":%d}",
                result.getR(),
                result.getAlpha(),
                result.getTargetPrice(),
                passengerCount
        );
    }
}
