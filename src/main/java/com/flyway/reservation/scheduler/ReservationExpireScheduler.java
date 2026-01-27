package com.flyway.reservation.scheduler;

import com.flyway.reservation.dto.ExpiredReservationView;
import com.flyway.reservation.repository.ReservationExpireRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationExpireScheduler {

    private final ReservationExpireRepository expireRepository;

    @Scheduled(fixedDelay = 60_000)  // 1분
    @Transactional
    public void expireReservations() {

        // 1. 만료된 HELD 예약 조회
        List<ExpiredReservationView> expiredList = expireRepository.findExpiredHeldReservations();

        if (expiredList.isEmpty()) {
            return;
        }

        Set<String> processedReservations = new HashSet<>();

        for (ExpiredReservationView item : expiredList) {

            // 2. 잔여석 복구
            int updated = expireRepository.incrementSeat(
                    item.getFlightId(),
                    item.getCabinClassCode(),
                    item.getPassengerCount()
            );

            if (updated > 0) {
                log.info("[ReservationExpire] 잔여석 복구: flightId={}, cabin={}, count={}",
                        item.getFlightId(), item.getCabinClassCode(), item.getPassengerCount());
            }

            // 3. 예약 상태 변경
            if (!processedReservations.contains(item.getReservationId())) {
                expireRepository.updateReservationStatus(item.getReservationId(), "EXPIRED");
                processedReservations.add(item.getReservationId());
                log.info("[ReservationExpire] 예약 만료 처리: reservationId={}", item.getReservationId());
            }
        }

        log.info("[ReservationExpire] 총 {}건 만료 처리 완료", processedReservations.size());
    }
}