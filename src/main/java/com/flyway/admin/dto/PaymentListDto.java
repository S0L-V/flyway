package com.flyway.admin.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentListDto {

	private String paymentId;
	private String reservationId;
	private String transactionId;
	private Long amount;
	private String paymentMethod;
	private String status;
	private LocalDateTime createdAt;
	private LocalDateTime paidAt;

	// 사용자 정보
	private String userId;
	private String userName;
	private String userEmail;

	// 항공편 정보
	private String flightNumber;
	private String route;

	/**
	 * 결제 수단 한글 변환
	 */
	public String getPaymentMethodDisplay() {
		if (paymentMethod == null) {
			return "-";
		}
		switch (paymentMethod) {
			case "CARD": return "카드";
			case "BANK_TRANSFER": return "계좌이체";
			case "KAKAO_PAY": return "카카오페이";
			case "NAVER_PAY": return "네이버페이";
			case "TOSS_PAY": return "토스페이";
			default: return paymentMethod;
		}
	}



	/**
	 * 상태 배지 색상
	 */
	public String getStatusBadgeClass() {
		if (status == null) {
			return "bg-gray-100 text-gray-800";
		}
		switch (status) {
			case "PENDING": return "bg-yellow-100 text-yellow-800";
			case "PAID": return "bg-green-100 text-green-800";
			case "FAILED": return "bg-red-100 text-red-800";
			case "CANCELLED": return "bg-gray-100 text-gray-800";
			case "REFUNDED": return "bg-orange-100 text-orange-800";
			default: return "bg-gray-100 text-gray-800";
		}
	}
}
