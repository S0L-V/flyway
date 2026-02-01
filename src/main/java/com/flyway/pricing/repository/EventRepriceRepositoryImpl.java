package com.flyway.pricing.repository;

import com.flyway.payment.mapper.RefundMapper;
import com.flyway.pricing.mapper.EventRepriceMapper;
import com.flyway.pricing.model.EventRepriceSegment;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class EventRepriceRepositoryImpl implements EventRepriceRepository {

    private final EventRepriceMapper eventRepriceMapper;
    private final RefundMapper refundMapper;

    @Override
    public List<EventRepriceSegment> findRepriceSegments(String reservationId) {
        return eventRepriceMapper.selectRepriceSegments(reservationId);
    }

    public int selectPassengerCount(String reservationId) {
        return refundMapper.selectPassengerCountByReservationId(reservationId);
    }

}
