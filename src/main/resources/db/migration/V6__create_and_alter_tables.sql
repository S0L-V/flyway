-- ============================================
-- visitor_log 테이블 생성
-- 방문자 추적을 위한 로그 테이블
-- ============================================

CREATE TABLE IF NOT EXISTS visitor_log (
                                           log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                           session_id VARCHAR(100) NOT NULL COMMENT 'JSESSIONID',
    user_id CHAR(36) NULL COMMENT '로그인 사용자 ID (nullable)',
    ip_address VARCHAR(45) NOT NULL COMMENT '클라이언트 IP',
    user_agent VARCHAR(500) COMMENT '브라우저 User-Agent',
    page_url VARCHAR(500) COMMENT '방문 페이지 URL',
    referer VARCHAR(500) COMMENT 'Referer URL',
    visited_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '방문 시각',

    INDEX idx_visited_at (visited_at),
    INDEX idx_session_date (session_id, visited_at),
    INDEX idx_user_id (user_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='방문자 로그';


-- ==============================================================
-- flight 인덱스 생성 및 airport 칼럼 추가
-- ===============================================================

CREATE INDEX idx_flight_search
    ON flight (
               departure_airport,
               arrival_airport,
               departure_time
        );

ALTER TABLE airport
    ADD COLUMN timezone VARCHAR(50);

/* =========================================================

aircraft_seat
한 항공기 내에서 좌석 번호는 중복될 수 없음
(예: 같은 aircraft_id에 12A가 두 개 존재하면 안 됨)
========================================================= */
ALTER TABLE aircraft_seat
    ADD CONSTRAINT uk_aircraft_seat
        UNIQUE (aircraft_id, seat_no);

/* =========================================================

passenger_seat
하나의 예약 구간(reservation_segment)에서
같은 승객(passenger)은 좌석을 하나만 가질 수 있음
→ 동일 승객의 중복 좌석 배정 방지
========================================================= */
ALTER TABLE passenger_seat
    ADD CONSTRAINT uk_passenger_seat_segment_passenger
        UNIQUE (reservation_segment_id, passenger_id);

/* =========================================================

passenger_seat
하나의 flight_seat(특정 항공편의 특정 좌석)는
오직 한 명의 승객에게만 할당 가능
→ 좌석 이중 배정(더블북킹) 방지
========================================================= */
ALTER TABLE passenger_seat
    ADD CONSTRAINT uk_passenger_seat_flight_seat
        UNIQUE (flight_seat_id);

