package com.flyway.rank.repository;

import com.flyway.rank.dto.AirportCountDto;

import java.util.List;

public interface RankRepository {
    // 공항 검색 수 삽입
    void insertSearchStats(String arrAirport);

    // 공항 순위 조회
    List<AirportCountDto> findLast7DaysCount();

    // 7일 전 데이터 삭제
    void deleteOldStats();
}
