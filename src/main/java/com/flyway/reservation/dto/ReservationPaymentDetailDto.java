package com.flyway.reservation.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ReservationPaymentDetailDto {

    private String reservationId;
    private String userId;
    private String tripType;
    private Integer passengerCount;
    private String reservationStatus;
    private LocalDateTime reservedAt;

    /* 가는 편 */
    private String outFlightNumber;
    private String outCabinClass;       // cabin_class_name
    private String outCabinClassCode;   // ECO/BIZ/FST
    private Long outPricePerPerson;     // snap_price
    private Long outTotal;              // snap_price * passengerCount

    /* 오는 편 (nullable) */
    private String inFlightNumber;
    private String inCabinClass;
    private String inCabinClassCode;
    private Long inPricePerPerson;
    private Long inTotal;

    /* 부가 서비스 */
    private Long baggageTotal;
    private Long mealTotal;
    private Long serviceTotal;

    /* 총 금액 */
    private Long totalAmount;
}
