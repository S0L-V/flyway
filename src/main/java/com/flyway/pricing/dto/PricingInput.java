package com.flyway.pricing.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PricingInput {

    /* ===== 식별자 (계산 로직에서 사용x) ===== */
    private String flightId;
    private String cabinClassCode; // ECO | BIZ | FST

    /* ===== 가격 ===== */
    private long basePrice;     // B
    private long currentPrice;  // P_old

    /* ===== 좌석 ===== */
    private int remainingSeats; // 잔여석
    private int totalSeats;     // 총 좌석 수

    /* ===== 시간 ===== */
    private LocalDateTime departureTime; // 출발 시각
    private LocalDateTime now;       // 계산 기준 시각 (테스트용 주입)

    /* ===== 실행 컨텍스트 ===== */
    private boolean eventBased;                // true = 결제 이벤트
    private LocalDateTime  lastEventPricedAt;  // 마지막 '이벤트 기반' 가격 반영 시각
}
