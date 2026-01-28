package com.flyway.payment.domain;

public enum ReservationStatus {

    HELD,       // 예약 임시 저장 (10분 유효)
    PAYING,     // 결제 진행 중 (추가!)
    CONFIRMED,  // 결제 완료, 예약 확정
    EXPIRED,    // 만료됨
    CANCELLED   // 취소됨

}
