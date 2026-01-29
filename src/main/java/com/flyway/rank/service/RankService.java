package com.flyway.rank.service;

import com.flyway.rank.dto.RankItemDto;

import java.util.List;

public interface RankService {
    void increaseRealtime(String airportId);

    List<RankItemDto> getCurrentRank();
}
