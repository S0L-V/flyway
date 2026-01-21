package com.flyway.search.service;

import com.flyway.search.domain.*;
import com.flyway.search.dto.FlightOptionDTO;
import com.flyway.search.dto.FlightSearchRequestDTO;
import com.flyway.search.dto.FlightSearchResponseDTO;
import com.flyway.search.dto.SearchResultDTO;
import com.flyway.search.mapper.FlightMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Primary
public class FlightServiceImpl implements FlightService{

    @Autowired
    FlightMapper mapper;

    @Override
    public List<FlightVO> list(FlightVO vo) {
        return mapper.list(vo);
    }

    @Override
    public List<AirportVO> airport(AirportVO vo) {
        return mapper.airport(vo);
    }

    @Override
    public SearchResultDTO search(FlightSearchRequestDTO dto) {
        List<FlightSearchResponseDTO> outbounds = mapper.outbound(dto);

        SearchResultDTO result = new SearchResultDTO();
        List<FlightOptionDTO> options = new ArrayList<>();

        // ✅ 편도
        if ("OW".equals(dto.getTripType())) {
            for (FlightSearchResponseDTO o : outbounds) {
                FlightOptionDTO opt = new FlightOptionDTO();
                opt.setOutbound(o);
                opt.setInbound(null);
//                opt.setLastSeats(o.getLastSeats());     // 편도는 그냥 outbound 기준
//                opt.setTotalPrice(null);                // 아직 운임 없으면 null
                options.add(opt);
            }
            result. setOptions(options);
            return result;
        }

        // ✅ 왕복
        List<FlightSearchResponseDTO> inbounds = mapper.inbound(dto);

        int limit = 100;  // ✅ 폭발 방지(원하는 숫자로)
        for (FlightSearchResponseDTO o : outbounds) {
            for (FlightSearchResponseDTO i : inbounds) {

                // (선택) 최소 필터: 시간/날짜 맞추기, 항공사 맞추기 등
                // 예: return편 출발일이 dto.dateEnd인지 확인 같은 것들
                if (!sameAirline(o.getFlightNumber(), i.getFlightNumber())) {
                    continue;
                }

                FlightOptionDTO opt = new FlightOptionDTO();

                opt.setOutbound(o);
                opt.setInbound(i);
                // lastSeats 예시: 왕복은 더 작은 쪽이 전체 좌석 제약
//                Integer seats = minNullable(o.getLastSeats(), i.getLastSeats());
//                opt.setLastSeats(seats);

                // totalPrice 예시(운임 붙이면): opt.setTotalPrice(oPrice + iPrice);
                opt.setTotalPrice(0);

                options.add(opt);

                if (options.size() >= limit) break;
            }
            if (options.size() >= limit) break;
        }

        result.setOptions(options);
        return result;
    }

    public boolean sameAirline(String a, String b) {
        if (a == null || b == null || a.length() < 2 || b.length() < 2) return false;
        return a.substring(0, 2).equalsIgnoreCase(b.substring(0, 2));
    }
}
