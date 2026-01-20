package com.flyway.search.service;

import com.flyway.search.domain.*;
import com.flyway.search.dto.*;
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
    public List<Flight> list(Flight vo) {
        return mapper.list(vo);
    }

    // 공항 토글
    @Override
    public List<Airport> airport(Airport vo) {
        return mapper.airport(vo);
    }

    // 검색 결과
    @Override
    public SearchResultDto search(FlightSearchRequest dto) {
        List<FlightSearchResponse> outbounds = mapper.outbound(dto);

        SearchResultDto result = new SearchResultDto();
        List<FlightOptionDto> options = new ArrayList<>();

        // 편도
        if ("OW".equals(dto.getTripType())) {
            for (FlightSearchResponse o : outbounds) {
                FlightOptionDto opt = new FlightOptionDto();
                opt.setOutbound(o);
                opt.setInbound(null);
                opt.setTotalSeats(o.getSeatCount());     // 좌석은 각각 total로 하려면 추가
//                opt.setTotalPrice(o.getCurrentPrice);                // 아직 운임 없으면 null
                options.add(opt);
            }
            result. setOptions(options);
            return result;
        }

        // 왕복
        List<FlightSearchResponse> inbounds = mapper.inbound(dto);

        int limit = 100;  // 폭발 방지(원하는 숫자로)
        for (FlightSearchResponse o : outbounds) {
            for (FlightSearchResponse i : inbounds) {

                // (선택) 최소 필터: 시간/날짜 맞추기, 항공사 맞추기 등
                // 예: return편 출발일이 dto.dateEnd인지 확인 같은 것들

                FlightOptionDto opt = new FlightOptionDto();

                opt.setOutbound(o);
                opt.setInbound(i);

                int seats = Math.min(o.getSeatCount(), i.getSeatCount());
                opt.setTotalSeats(seats);

                // totalPrice
//                if (sameAirline(o.getFlightNumber(), i.getFlightNumber())) {
//                    double Price = (o.getCurrentPrice() + i.getCurrentPrice()) * 10 / 14;
//                    int totalPrice = (int) ((price + 50) / 100 * 100)   // 100단위로
//                    opt.setTotalPrice(totalPrice);
//                } else {
//                    int totalPrice = o.getCurrentPrice() + i.getCurrentPrice();
//                    opt.setTotalPrice(totalPrice);
//                }

                opt.setTotalPrice(0); // 임시 가격

                options.add(opt);

                if (options.size() >= limit) break;
            }
            if (options.size() >= limit) break;
        }

        result.setOptions(options);
        return result;
    }

    // 왕복 같은 항공사인지
    public boolean sameAirline(String a, String b) {
        if (a == null || b == null || a.length() < 2 || b.length() < 2) return false;
        return a.substring(0, 2).equalsIgnoreCase(b.substring(0, 2));
    }
}
