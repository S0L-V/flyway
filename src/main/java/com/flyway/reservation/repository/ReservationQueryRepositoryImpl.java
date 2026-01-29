package com.flyway.reservation.repository;

import com.flyway.reservation.dto.ReservationDetailDto;
import com.flyway.reservation.dto.ReservationSummaryDto;
import com.flyway.reservation.mapper.ReservationQueryMapper;
import com.flyway.template.common.PageInfo;
import com.flyway.template.common.PageResult;
import com.flyway.template.common.Paging;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ReservationQueryRepositoryImpl implements ReservationQueryRepository {

    private final ReservationQueryMapper reservationQueryMapper;

    @Override
    public PageResult<ReservationSummaryDto> findUserReservationHistories(String userId, Paging paging) {
        Paging safePaging = paging.safe();

        long totalCount = reservationQueryMapper.countReservationHistoriesByUserId(userId);

        List<ReservationSummaryDto> items = reservationQueryMapper.findReservationHistoriesByUserId(
                userId,
                safePaging.getOffset(),
                safePaging.getSize()
        );

        PageInfo pageInfo = PageInfo.of(safePaging.getPage(), safePaging.getSize(), totalCount);
        return PageResult.of(items, pageInfo);
    }

    @Override
    public ReservationDetailDto findUserReservationDetail(String userId, String reservationId) {
        return reservationQueryMapper.findReservationDetail(userId, reservationId);
    }
}
