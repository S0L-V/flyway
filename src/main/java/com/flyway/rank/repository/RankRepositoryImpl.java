package com.flyway.rank.repository;

import com.flyway.rank.dto.AirportCountDto;
import com.flyway.rank.mapper.RankMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class RankRepositoryImpl implements RankRepository {
    private final RankMapper mapper;
    // 공항 검색 수 삽입
    public void insertSearchStats(String airportId, Integer count){ mapper.insertSearchStats(airportId, count);}

    // 공항 순위 조회
    public List<AirportCountDto> findLast7DaysCount() { return mapper.findLast7DaysCount(); }

    // 7일 전 데이터 삭제
    public void deleteOldStats() {};
}
