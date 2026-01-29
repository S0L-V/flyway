package com.flyway.rank.service;

import com.flyway.rank.dto.RankItemDto;
import com.flyway.search.domain.Airport;

import java.util.List;

public interface RankService {
    void increaseRealtime(String airportId);

    List<RankItemDto> getCurrentRank();
}
