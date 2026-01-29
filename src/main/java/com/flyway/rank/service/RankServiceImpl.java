package com.flyway.rank.service;

import com.flyway.rank.dto.RankItemDto;
import com.flyway.rank.repository.RankRepository;
import com.flyway.search.domain.Airport;
import com.flyway.search.repository.FlightRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class RankServiceImpl implements RankService {
    private final RankRepository rankRepository;
    private final FlightRepository flightRepository;
    // 7일 데이터 db에서 가져와서 저장
    Map<String, Integer> base7DaysCount = new HashMap<>();

    // 실시간 데이터
    Map<String, Integer> realTimeCount = new ConcurrentHashMap<>();

    // 공항
    Map<String, Airport> airportInfoCache = new HashMap<>();

    // 전, 현순위 캐시
    volatile List<RankItemDto> currentRankCache = new ArrayList<>();
    volatile Map<String, Integer> previousRankIndex = new HashMap<>();

    @PostConstruct
    public void init() {
        loadAirportInfo(new Airport());
        loadBase7DaysCount();
        calculateRank();
    }

    private void loadAirportInfo(Airport vo) {
        airportInfoCache.clear();
        flightRepository.findDepAirports(vo)
                .forEach(a -> airportInfoCache.put(a.getAirportId(), a));
    }

    private void loadBase7DaysCount() {
        base7DaysCount.clear();
        rankRepository.findLast7DaysCount()
                .forEach(dto ->
                        base7DaysCount.put(dto.getAirportId(), dto.getSearchCount())
                );
    }

    @Override
    public void increaseRealtime(String airportId) {
        realTimeCount.merge(airportId, 1, Integer::sum);
    }

    private void calculateRank() {
        Map<String, Integer> merged = new HashMap<>(base7DaysCount);

        realTimeCount.forEach((k, v) ->
            merged.merge(k, v, Integer::sum)
        );

        List<RankItemDto> newRank = merged.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(6)
                .map((entry) -> {
                    Airport airport = airportInfoCache.get(entry.getKey());

                    RankItemDto dto = new RankItemDto();
                    dto.setAirportId(entry.getKey());
                    dto.setSearchCount(entry.getValue());
                    dto.setCity(airport.getCity());
                    dto.setImageUrl(airport.getImageUrl());
                    return dto;
                })
                .toList();
        Map<String, Integer> newIndex = new HashMap<>();
        for(int i = 0; i < newRank.size(); i++) {
            RankItemDto dto = newRank.get(i);
            dto.setRank(i + 1);

            Integer prev = previousRankIndex.get(dto.getAirportId());
            dto.setDiff(prev == null ? 0 : prev - (i + 1));

            newIndex.put(dto.getAirportId(), i + 1);
        }

        previousRankIndex = newIndex;
        currentRankCache = newRank;
    }

    @Scheduled(fixedDelay = 30000)
    public void refreshRankCache() {
        calculateRank();
    }

    @Override
    public List<RankItemDto> getCurrentRank() {
        return currentRankCache;
    }
}
