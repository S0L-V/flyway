package com.flyway.reservation.service;

import com.flyway.reservation.dto.ReservationSummaryDto;
import com.flyway.reservation.repository.ReservationQueryRepository;
import com.flyway.template.common.PageResult;
import com.flyway.template.common.Paging;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReservationQueryServiceImpl implements ReservationQueryService {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 50;

    private final ReservationQueryRepository reservationQueryRepository;

    @Override
    public PageResult<ReservationSummaryDto> getUserReservationHistories(
            String userId,
            Integer page,
            Integer size
    ) {
        Paging paging = Paging.of(page, size, DEFAULT_PAGE, DEFAULT_SIZE, MAX_SIZE);
        return reservationQueryRepository.findUserReservationHistories(userId, paging);
    }
}
