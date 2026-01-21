package com.flyway.seat.scheduler;

import com.flyway.seat.service.SeatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SeatHoldScheduler {

    private static final Logger log = LoggerFactory.getLogger(SeatHoldScheduler.class);

    private final SeatService seatService;

    public SeatHoldScheduler(SeatService seatService) {
        this.seatService = seatService;
    }

    /* 예를 들어 HOLD 유효시간이 10분이고, 배치가 1분마다 실행되면

    - 10:00:00에 HOLD 걸림 → hold_expires_at = 10:10:00

    - 좌석은 10:10:00에 만료됐지만

    - 배치는 1분마다라서 10:10:00~10:10:59 사이엔 DB에 HOLD로 남아있을 수 있음

    - 다음 배치가 10:11:00에 돌면 그때 AVAILABLE로 복구됨 */
    @Scheduled(fixedDelay = 60_000)
    public void releaseExpiredHoldsJob() {
        int updated = seatService.releaseExpiredHolds();

        if (updated > 0) {
            log.info("released expired HOLD seats: {}", updated);
        }
    }
}
