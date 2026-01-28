package com.flyway.payment.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties
public class TossPaymentResponse {

    private String paymentKey;      // 결제 키
    private String orderId;         // 주문 ID
    private String orderName;       // 주문명
    private String status;          // 결제 상태 (DONE, CANCELED, EXPIRED 등)
    private String method;          // 결제 수단 (카드, 계좌이체, 간편결제)
    private Long totalAmount;       // 총 결제 금액
    private Long balanceAmount;     // 잔액 (부분 취소 시 사용)
    private String approvedAt;      // 결제 승인 시각
    private String requestedAt;     // 결제 요청 시각

    // 결제 구분
    private CardInfo card;
    private TransferInfo transfer;
    private EasyPayInfo easyPay;


     //카드 결제 상세 정보
    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CardInfo {
        private String company;          // 카드사
        private String number;           // 카드번호 (마스킹)
        private String cardType;         // 카드 타입 (신용, 체크)
        private String ownerType;        // 소유자 타입 (개인, 법인)
        private Integer installmentPlanMonths;  // 할부 개월
        private String approveNo;        // 승인 번호
    }

    //계좌이체 상세 정보
    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TransferInfo {
        private String bankCode;         // 은행 코드
        private String settlementStatus; // 정산 상태
    }

    // 간편결제 상세 정보
    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EasyPayInfo {
        private String provider;         // 결제사
        private Long amount;             // 결제 금액
        private Long discountAmount;     // 할인 금액
    }

}

/*
결제 구분(카드, 계좌, 간편)
카드 결제, 계좌이체, 간편결제 상세정보
 */