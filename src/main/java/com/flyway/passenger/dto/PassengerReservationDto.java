package com.flyway.passenger.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PassengerReservationDto {

    /* 탑승객 */
    private String passengerId;
    private String reservationId;
    private String krFirstName;
    private String krLastName;
    private String firstName;
    private String lastName;
    private LocalDate birth;
    private String gender;
    private String email;
    private String phoneNumber;
    private String checkedBaggageApplied;

    /* 여권  */
    private String passportNo;
    private String passportIssueCountry;
    private LocalDate passportExpiryDate;
    private String passportStatus;
    private LocalDateTime passportSubmittedAt;
    private LocalDateTime passportReminderSentAt;

    /* 예약 구간 */
    private String reservationSegmentId;
    private Integer segmentOrder;

    /* 좌석 */
    private String passengerSeatId;
    private String flightSeatId;

    /* 부가 서비스 */
    private String passengerServiceId;
    private String serviceType;     // 0 (수하물) | 1 (기내식)
    private String tripType;        // 0 (OW) | 1 (RT)
    private Integer quantity;
    private Long totalPrice;
    private String mealId;
    private String policyId;
    private String serviceDetails; // JSON
    private LocalDateTime addedAt;
}
