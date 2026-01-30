package com.flyway.passenger.dto;

import lombok.*;

/**
 * passenger_service INSERT용 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PassengerServiceInsertDTO {

    private String psId;                    // UUID
    private String passengerId;
    private String reservationSegmentId;
    private String mealId;                  // 기내식만 (수하물은 null)
    private String policyId;                // baggage_policy FK
    private String serviceType;             // "0" 수하물 / "1" 기내식
    private int quantity;
    private Long totalPrice;
    private String serviceDetails;          // JSON
    private String tripType;                // OW / RT
}