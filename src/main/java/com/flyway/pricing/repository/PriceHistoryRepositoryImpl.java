package com.flyway.pricing.repository;

import com.flyway.pricing.mapper.PriceHistoryMapper;
import com.flyway.pricing.model.PriceHistoryInsert;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PriceHistoryRepositoryImpl implements PriceHistoryRepository {

    private final PriceHistoryMapper priceHistoryMapper;

    public int insertEventHistory(PriceHistoryInsert dto) {
        return priceHistoryMapper.insertEventHistory(dto);
    }

}
