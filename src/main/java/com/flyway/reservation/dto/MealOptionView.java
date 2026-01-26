package com.flyway.reservation.dto;

import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MealOptionView {

    private String mealId;
    private String mealName;
    private String availableForClass;   // ECO / BIZ / FST 형태
    private String imageUrl;
    private String isActive;            // Y / N
    private String routeType;           // INTERNATIONAL
}