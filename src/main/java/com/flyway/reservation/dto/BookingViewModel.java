package com.flyway.reservation.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingViewModel {

    private String reservationId;
    private int passengerCount;
    private String status;      // HELD/CONFIRMED/EXPIRED
    private String tripType;    // 0 OW | 1 RT
    private LocalDateTime expiredAt;

    // 버튼 활성화 여부
    private boolean passengerSaved;
    //구간 스냅샷
    private List<ReservationSegmentView> segments;
    // 탑승자 입력/조회용
    private List<PassengerView> passengers;
}
