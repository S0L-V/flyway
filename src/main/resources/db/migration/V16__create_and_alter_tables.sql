-- 검색 통계 테이블
CREATE TABLE airport_search_stats (
      airport_id    VARCHAR(20) NOT NULL COMMENT 'ICN 이런식',
      std_date      DATE        NOT NULL,
      search_count  INT         NOT NULL DEFAULT 0,

      CONSTRAINT PK_AIRPORT_SEARCH_STATS
          PRIMARY KEY (airport_id, std_date),

      CONSTRAINT FK_airport_TO_airport_search_stats
          FOREIGN KEY (airport_id)
          REFERENCES airport (airport_id)
) ENGINE=InnoDB;

-- 공항테이블 이미지 url 칼럼 추가
ALTER TABLE airport
    ADD COLUMN image_url VARCHAR(500) NULL
COMMENT '도시 이미지 URL';

-- 특가 항공권 프로모션 테이블 (리팩토링)
CREATE TABLE promotion (
       promotion_id CHAR(36) PRIMARY KEY,
       flight_id CHAR(36) NOT NULL COMMENT 'FK to flight table',
       title VARCHAR(100) NOT NULL COMMENT '프로모션 제목 (예: 4인 가족 제주 특가)',
       passenger_count INT NOT NULL DEFAULT 1 COMMENT '프로모션에 적용되는 인원수',
       discount_percentage INT NOT NULL COMMENT '할인율 (예: 20 for 20%)',
       cabin_class_code VARCHAR(10) NOT NULL DEFAULT 'ECO' COMMENT '대상 좌석 클래스 (ECO로 고정)',
       tags VARCHAR(200) COMMENT '태그 (콤마 구분)',
       is_active CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '활성화 여부',
       display_order INT NOT NULL DEFAULT 0 COMMENT '표시 순서 (낮을수록 먼저)',
       created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
       updated_at DATETIME ON UPDATE CURRENT_TIMESTAMP,
       created_by CHAR(36) COMMENT '생성한 관리자 ID',

       INDEX idx_promotion_active (is_active),
       INDEX idx_promotion_order (display_order),
       CONSTRAINT fk_promotion_flight FOREIGN KEY (flight_id) REFERENCES flight(flight_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='메인페이지 특가 항공권 프로모션';