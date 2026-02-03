package com.flyway.payment.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * 메인 페이지 실시간 결제 티커용 DTO
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentPaymentTickerDto {
	private String maskedEmail;      // kim***@gmail.com
	private String passengerName;    // 김**
	private String departureAirport; // ICN
	private String arrivalAirport;   // NRT
	private Long amount;             // 189000
	private LocalDateTime paidAt;    // 결제 시간
}
