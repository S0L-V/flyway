package com.flyway.sender.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PassengerTicketInfo {

    private String passengerId;
    private String firstName;
    private String lastName;
    private String phoneNumber;

    // 구간 정보
    private String reservationSegmentId;
    private Integer segmentOrder;

    // 좌석 정보
    private String seatNo;

    // 부가서비스
    private String serviceType;
    private String mealName;
    private String serviceDetails;
}