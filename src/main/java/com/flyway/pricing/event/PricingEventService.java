package com.flyway.pricing.event;

import java.time.Instant;
import java.util.List;

public interface PricingEventService {

    /**
     * 결제 완료(좌석 확정) 이후 이벤트 기반 재가격을 수행한다.
     *
     * @param reservationId 결제와 연결된 예약 ID (segments 조회의 기준)
     * @param paymentId 결제 ID (price_history 원인 추적/감사 로그용)
     * @return 처리 결과 요약
     */
    RepriceSummary repriceAfterPayment(String reservationId, String paymentId);

    /**
     * 환불 완료(좌석 원복) 이후 이벤트 기반 재가격을 수행한다.
     */
    RepriceSummary repriceAfterRefund(String reservationId, String refundId);

    // -----------------------
    // 결과 DTO
    // -----------------------
    record RepriceSummary(
            String reservationId,
            String triggerId,          // paymentId / refundId
            EventType eventType,
            int candidates,            // 재가격 시도 대상 (segment 수)
            int applied,               // 실제 가격 변경 반영 건수
            int skipped,
            Instant calculatedAt,
            List<RepriceItemResult> items
    ) {}

    record RepriceItemResult(
            String flightId,
            String cabinClassCode,

            Long oldPrice,
            Long newPrice,
            String reason              // 로그/디버깅용 텍스트
    ) {}

    enum EventType { PAYMENT, REFUND }

}
