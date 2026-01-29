package com.flyway.rank.mapper;

import com.flyway.rank.dto.AirportCountDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RankMapper {
    // 공항 검색 수 삽입
    void insertSearchStats(@Param("arrAirport") String arrAirport);

    // 공항 순위 조회
    List<AirportCountDto> findLast7DaysCount();

    // 7일 전 데이터 삭제
    void deleteOldStats();
}
