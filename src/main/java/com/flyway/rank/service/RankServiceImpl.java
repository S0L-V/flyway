package com.flyway.rank.service;

import com.flyway.rank.dto.RankItemDto;
import com.flyway.rank.repository.RankRepository;
import com.flyway.search.domain.Airport;
import com.flyway.search.repository.FlightRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
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
    // 서버 시작 시 실행
    public void init() {
        loadAirportInfo(new Airport());
        loadBase7DaysCount();

        List<RankItemDto> initRank = calculateRank();
        forceApplyRank(initRank);
    }

    // 시작 시 초기화
    private void forceApplyRank(List<RankItemDto> rank) {
        Map<String, Integer> newIndex = new HashMap<>();

        for(int i = 0; i < rank.size(); i++) {
            RankItemDto dto = rank.get(i);
            dto.setRank(i + 1);
            dto.setDiff(0);
            dto.setNew(false);
            newIndex.put(dto.getAirportId(), i + 1);
        }

        previousRankIndex = newIndex;
        currentRankCache = rank;
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

        updateRank();
    }

    private List<RankItemDto> calculateRank() {
        Map<String, Integer> merged = new HashMap<>(base7DaysCount);

        realTimeCount.forEach((k, v) ->
            merged.merge(k, v, Integer::sum)
        );

        return merged.entrySet().stream()
                .map((entry) -> {
                    Airport airport = airportInfoCache.get(entry.getKey());

                    RankItemDto dto = new RankItemDto();
                    dto.setAirportId(entry.getKey());
                    dto.setSearchCount(entry.getValue());
                    dto.setCity(airport.getCity());
                    dto.setImageUrl(airport.getImageUrl());
                    return dto;
                })
                .sorted(
                        Comparator
                            .comparing(RankItemDto::getSearchCount).reversed()
                            .thenComparing(RankItemDto::getAirportId)
                )
                .limit(6)
                .toList();
    }

    private synchronized void updateRank() {
        List<RankItemDto> newRank = calculateRank();

        boolean changed = false;

        for(int i = 0; i < newRank.size(); i++) {
            String airportId = newRank.get(i).getAirportId();
            Integer prev = previousRankIndex.get(airportId);

            if(prev == null || prev != i + 1) {
                changed = true;
                break;
            }
        }

        if (!changed) return;

        Map<String, Integer> newIndex = new HashMap<>();
        for(int i = 0; i < newRank.size(); i++) {
            RankItemDto dto = newRank.get(i);
            dto.setRank(i + 1);

            Integer prev = previousRankIndex.get(dto.getAirportId());
            if (prev == null || prev > 6) {
                dto.setDiff(0);
                dto.setNew(true);
            } else {
                dto.setDiff(prev - (i + 1));
                dto.setNew(false);
            }

            newIndex.put(dto.getAirportId(), i + 1);
        }

        previousRankIndex = newIndex;
        currentRankCache = newRank;
    }

    @Scheduled(fixedDelay = 30000)
    public void refreshRankCache() {
        updateRank();
    }

    @Override
    public List<RankItemDto> getCurrentRank() {
        return currentRankCache;
    }
}
