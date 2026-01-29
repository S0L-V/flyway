package com.flyway.rank.dto;

import lombok.Data;

@Data
public class RankItemDto {
    private String airportId;
    private String city;
    private String imageUrl;

    private int searchCount;  // 7일 + 실시간 합계
    private int rank;
    private int diff;  // 순위 변동
}
