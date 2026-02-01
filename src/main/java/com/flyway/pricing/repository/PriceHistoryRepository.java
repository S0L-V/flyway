package com.flyway.pricing.repository;

import com.flyway.pricing.model.PriceHistoryInsert;

public interface PriceHistoryRepository {
    int insertEventHistory(PriceHistoryInsert dto);
}
