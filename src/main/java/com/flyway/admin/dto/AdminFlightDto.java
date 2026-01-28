package com.flyway.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 관리자용 항공편 DTO (원본 Flight 도메인 수정 없이 사용)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminFlightDto {
	private String flightId;
	private String departureAirport;
	private String arrivalAirport;
	private LocalDateTime departureTime;
	private LocalDateTime arrivalTime;
	private String terminalNo;
	private String flightNumber;
	private String routeType;
	private String airlineId;
	private String airlineName;
}
