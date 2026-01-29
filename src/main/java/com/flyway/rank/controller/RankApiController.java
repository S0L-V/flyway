package com.flyway.rank.controller;

import com.flyway.rank.dto.RankItemDto;
import com.flyway.rank.service.RankService;
import lombok.AllArgsConstructor;
import org.apache.poi.ss.formula.functions.Rank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
public class RankApiController {
    private final RankService rankService;

    @GetMapping("/api/public/rank/realtime")
    public List<RankItemDto> getRealtimeRank() {
        return rankService.getCurrentRank();
    }
}
