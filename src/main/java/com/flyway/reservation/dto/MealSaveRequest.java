package com.flyway.reservation.dto;

import lombok.*;
import java.util.List;

/**
 * 기내식 저장 요청 DTO (JSON)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MealSaveRequest {

    private List<PassengerMeal> items;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PassengerMeal {
        private String passengerId;
        private String reservationSegmentId;
        private String mealId;              // 선택한 기내식 (null이면 선택 안함)
    }
}