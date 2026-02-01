package com.flyway.pricing.mapper;

import com.flyway.pricing.model.PriceHistoryInsert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PriceHistoryMapper {
    int insertEventHistory(PriceHistoryInsert dto);
}
