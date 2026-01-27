package com.flyway.admin.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.flyway.template.domain.BaseEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 특가 항공권 프로모션 DTO
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionDto extends BaseEntity {

	private String promotionId;
	private String flightId;
	private String title;
	private int passengerCount;
	private int discountPercentage;
	private String cabinClassCode;
	private String tags;
	private String isActive;
	private int displayOrder;
	private String createdBy;

	// flight JOIN
	private String departureAirportName;
	private String arrivalAirportName;
	private String airlineName;
	private LocalDateTime departureTime;
	private LocalDateTime arrivalTime;

	// 계산 필드
	private long originalPrice; // 1인당 원가
	private long salePrice; // 1인당 할인가
	private long totalOriginalPrice; // (원가 * 인원수)
	private long totalSalePrice; // (할인가 * 인원수)

	/**
	 * 최종 할인된 총액 계산
	 * 서비스 계층에서 currentPrice (1인당 현재가)조회한 후 호출
	 * @param currentPrice 1인당 현재 가격
	 */
	public void calculatePrices(long currentPrice) {
		if (passengerCount <= 0 || discountPercentage < 0 || discountPercentage > 100) {
			// 잘못된 데이터는 0으로 처리
			this.originalPrice = 0;
			this.salePrice = 0;
			this.totalOriginalPrice = 0;
			this.totalSalePrice = 0;
			return;
		}

		BigDecimal original = BigDecimal.valueOf(currentPrice);
		BigDecimal discountRate = BigDecimal.valueOf(100 - discountPercentage).divide(BigDecimal.valueOf(100));

		this.originalPrice = original.longValue();
		this.salePrice = original.multiply(discountRate).longValue();
		this.totalOriginalPrice = original.multiply(BigDecimal.valueOf(passengerCount)).longValue();
		this.totalSalePrice = BigDecimal.valueOf(this.salePrice)
			.multiply(BigDecimal.valueOf(passengerCount))
			.longValue();
	}
}
