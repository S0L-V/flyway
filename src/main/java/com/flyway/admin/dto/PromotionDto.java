package com.flyway.admin.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
	private String flightNumber;
	private String departureAirport;
	private String arrivalAirport;
	private LocalDateTime departureTime;
	private LocalDateTime arrivalTime;

	// airport JOIN
	private String departureAirportName;  // dep.city
	private String arrivalAirportName;    // arr.city
	private String imageUrl;              // arr.image_url (도착지 이미지)

	// airline JOIN
	private String airlineName;
	private String airlineLogoUrl;

	// 계산
	private long originalPrice;      // 1인당 원가 (flight_seat_price.current_price)
	private long salePrice;          // 1인당 할인가
	private long totalOriginalPrice; // (원가 * 인원수)
	private long totalSalePrice;     // (할인가 * 인원수)

	/**
	 * 태그 리스트 반환
	 */
	public List<String> getTagList() {
		if (tags == null || tags.trim().isEmpty()) {
			return Collections.emptyList();
		}
		return Arrays.asList(tags.split(","));
	}

	/**
	 * 노선 표시 (ICN → NHA)
	 */
	public String getRouteDisplay() {
		return departureAirport + " → " + arrivalAirport;
	}

	/**
	 * 최종 할인된 총액을 계산합니다.
	 * 이 메서드는 서비스 계층에서 currentPrice (1인당 현재가)를 조회한 후 호출됩니다.
	 * @param currentPrice 1인당 현재 가격
	 */
	public void calculatePrices(long currentPrice) {
		if (passengerCount <= 0 || discountPercentage < 0 || discountPercentage > 100) {
			this.originalPrice = 0;
			this.salePrice = 0;
			this.totalOriginalPrice = 0;
			this.totalSalePrice = 0;
			return;
		}

		BigDecimal original = BigDecimal.valueOf(currentPrice);
		BigDecimal discountRate = BigDecimal.valueOf(100 - discountPercentage)
			.divide(BigDecimal.valueOf(100));

		this.originalPrice = original.longValue();
		this.salePrice = original.multiply(discountRate).longValue();
		this.totalOriginalPrice = original.multiply(BigDecimal.valueOf(passengerCount)).longValue();
		this.totalSalePrice = BigDecimal.valueOf(this.salePrice)
			.multiply(BigDecimal.valueOf(passengerCount)).longValue();
	}
}
