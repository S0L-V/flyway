package com.flyway.reservation.dto;

import lombok.*;

/**
 * 탑승객 선택한 부가서비스
 * 탑승객 테이블 조회
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PassengerServiceView {

    private String psId;
    private String passengerId;
    private String reservationSegmentId;
    private String mealId;
    private String policyId;
    private String serviceType;
    private int quantity;
    private int totalPrice;
    private String serviceDetails;
    private String tripType; //OW RT
    private String addedAt;

    private String mealName;
    private String mealImageUrl;





}
