package com.flyway.reservation.dto;

import com.flyway.passenger.dto.PassengerServiceView;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 부가서비스 팝업 JSP용 통합 ViewModel
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServicePopupViewModel {

    private String reservationId;
    private String tripType;                // "0" = 편도, "1" = 왕복

    // 구간 정보 (편도 1개, 왕복 2개)
    private List<SegmentServiceInfo> segments;

    // 탑승자 목록 + 현재 선택된 서비스
    private List<PassengerServiceInfo> passengers;

    // 수하물 정책 (등급/노선별)
    private BaggagePolicyView baggagePolicy;

    // 기내식 옵션 목록
    private List<MealOptionView> mealOptions;

    // 기내식 제공 여부 (국제선만 true)
    private boolean mealAvailable;

    // ─────────────────────────────────────────
    // 내부 클래스: 구간 정보
    // ─────────────────────────────────────────
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SegmentServiceInfo {
        private String reservationSegmentId;
        private int segmentOrder;               // 1 = 가는편, 2 = 오는편
        private String snapDepartureAirport;
        private String snapArrivalAirport;
        private LocalDateTime snapDepartureTime;
        private LocalDateTime snapArrivalTime;
        private String snapFlightNumber;
        private String snapCabinClassCode;      // ECO / BIZ / FST
        private String routeType;               // DOMESTIC / INTERNATIONAL
    }

    // ─────────────────────────────────────────
    // 내부 클래스: 탑승자별 서비스 정보
    // ─────────────────────────────────────────
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PassengerServiceInfo {
        private String passengerId;
        private String krFirstName;
        private String krLastName;
        private String firstName;
        private String lastName;

        // 구간별 수하물 서비스
        private List<PassengerServiceView> baggageServices;

        // 구간별 기내식 서비스
        private List<PassengerServiceView> mealServices;
    }
}