package com.flyway.pricing.batch.row;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 가격 갱신 대상(Flight ID) 데이터를 담을 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RepriceCandidateRow {
    // PK (Composite Key)
    private String flightId;
    private String cabinClassCode;

    // 가격 정보
    private long basePrice;     // B
    private long currentPrice;  // P_old
    private LocalDateTime lastEventPricedAt; // 쿨다운 체크용

    // 좌석 정보 (점유율 확인)
    private int remainingSeats;
    private int totalSeats;

    // 시간 정보 (Flight 테이블 Join)
    private LocalDateTime departureTime;
}