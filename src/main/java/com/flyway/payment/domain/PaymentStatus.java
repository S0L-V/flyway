package com.flyway.payment.domain;

public enum PaymentStatus {
    PENDING,            // 결제 대기 (토스 API 호출 전)
    PAID,               // 결제 완료
    FAILED,             // 결제 실패
    CANCELLED,          // 전액 취소
    PARTIAL_REFUNDED    // 부분 환불
}