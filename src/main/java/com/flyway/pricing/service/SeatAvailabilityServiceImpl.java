package com.flyway.pricing.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.Map;

@Service
@Slf4j
public class SeatAvailabilityServiceImpl implements SeatAvailabilityService {

    private final JdbcTemplate jdbcTemplate;

    public SeatAvailabilityServiceImpl(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public SeatStatus getSeatStatus(String flightId, String cabinClassCode) {
        // 1. flight_info(잔여석)와 aircraft(총 좌석)를 조인하여 조회
        // flight_info에는 '_seat'(단수), aircraft에는 '_seats'(복수) 컬럼명을 사용함
        String sql =
                "SELECT " +
                        "  fi.economy_class_seat   AS rem_eco, " +
                        "  fi.business_class_seat  AS rem_biz, " +
                        "  fi.first_class_seat     AS rem_fst, " +
                        "  ac.economy_class_seats  AS tot_eco, " +
                        "  ac.business_class_seats AS tot_biz, " +
                        "  ac.first_class_seats    AS tot_fst " +
                        "FROM flight_info fi " +
                        "INNER JOIN aircraft ac ON fi.aircraft_id = ac.aircraft_id " +
                        "WHERE fi.flight_id = ?";

        try {
            Map<String, Object> res = jdbcTemplate.queryForMap(sql, flightId);

            // 2. 요청된 등급(cabinClassCode)에 따라 적절한 컬럼 매핑
            int remaining = 0;
            int total = 0;

            switch (cabinClassCode) {
                case "ECO":
                    remaining = getInt(res, "rem_eco");
                    total = getInt(res, "tot_eco");
                    break;
                case "BIZ":
                    remaining = getInt(res, "rem_biz");
                    total = getInt(res, "tot_biz");
                    break;
                case "FST":
                    remaining = getInt(res, "rem_fst");
                    total = getInt(res, "tot_fst");
                    break;
                default:
                    throw new IllegalArgumentException("Unknown cabin class: " + cabinClassCode);
            }

            return new SeatStatus(total, remaining);

        } catch (Exception e) {
            // 에러 발생 시 로그를 남기고 (0,0)을 반환하여 배치가 멈추지 않게 처리
            log.error("좌석 정보 조회 실패 (Skip 처리됨) - flightId: {}, class: {}, error: {}",
                    flightId, cabinClassCode, e.getMessage());

            // 계산기(Calculator)에서 스킵 처리(INVALID_SEATS)
            return new SeatStatus(0, 0);
        }
    }

    // Null Safe Integer 변환 헬퍼
    private int getInt(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? ((Number) val).intValue() : 0;
    }
}
