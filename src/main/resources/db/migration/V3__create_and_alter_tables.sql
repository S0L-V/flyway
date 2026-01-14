-- 칼럼명 수정, ON UPDATE 사용x
ALTER TABLE flight_seat_price
    CHANGE COLUMN last_updated last_priced_at DATETIME NOT NULL
        DEFAULT CURRENT_TIMESTAMP
    COMMENT '판매가(current_price) 갱신 시각 (배치/이벤트 포함)';

-- 이벤트(결제/예매/취소 등) 기반 재계산의 마지막 수행 시각(쿨다운 판단용)
ALTER TABLE flight_seat_price
    ADD COLUMN last_event_priced_at DATETIME NULL
  COMMENT '이벤트 기반 재계산의 최종 수행시각 (쿨다운 판단용)';

-- price_history 원인 구분
ALTER TABLE price_history
    ADD COLUMN update_type VARCHAR(20) NOT NULL DEFAULT 'BATCH'
    COMMENT 'INIT | BATCH | EVENT | SNAPSHOT | ADMIN';

-- unique key : {항공편 + 좌석등급}은 1개의 row만 존재한다.
ALTER TABLE flight_seat_price
    ADD CONSTRAINT uq_fsp_flight_cabin UNIQUE (flight_id, cabin_class_code);

-- 가격 범위 체크
ALTER TABLE flight_seat_price
    ADD CONSTRAINT chk_fsp_price_nonneg CHECK (current_price >= 0 AND base_price >= 0);

-- price_history 기본 인덱스 (그래프 조회 성능)
CREATE INDEX idx_ph_flight_cabin_time
    ON price_history (flight_id, cabin_class_code, calculated_at);


ALTER TABLE reservation_segment
    ADD COLUMN snap_cabin_class_code VARCHAR(10) NOT NULL
    COMMENT '검색에서 선택한 등급 스냅샷(ECO/BIZ/FST)';


SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS email_verification_token;
DROP TABLE IF EXISTS user_identity;
DROP TABLE IF EXISTS user_profile;
DROP TABLE IF EXISTS users;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE users (
                       user_id CHAR(36) NOT NULL,
                       email VARCHAR(320) NULL,
                       password_hash VARCHAR(255) NULL,
                       status VARCHAR(20) NOT NULL COMMENT 'ACTIVE | BLOCKED | ONBOARDING(OAuth 인증만 완료)',
                       created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       PRIMARY KEY (user_id)
);

CREATE TABLE user_profile (
                              user_id CHAR(36) NOT NULL COMMENT 'PK,FK (1:1)',
                              passport_no VARCHAR(20) NULL,
                              country VARCHAR(100) NULL,
                              gender CHAR(1) NULL COMMENT 'M/F',
                              first_name VARCHAR(100) NULL,
                              last_name VARCHAR(100) NULL,
                              name VARCHAR(100) NULL,
                              PRIMARY KEY (user_id),
                              CONSTRAINT fk_user_profile_user
                                  FOREIGN KEY (user_id) REFERENCES users(user_id)
                                      ON DELETE CASCADE
);

CREATE TABLE user_identity (
                               user_identity_id CHAR(36) NOT NULL,
                               user_id CHAR(36) NOT NULL,
                               provider VARCHAR(100) NOT NULL COMMENT 'EMAIL | KAKAO | NAVER | GOOGLE | APPLE',
                               provider_user_id VARCHAR(255) NOT NULL COMMENT 'EMAIL-email | OAuth-고유ID',
                               created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               PRIMARY KEY (user_identity_id),
                               UNIQUE KEY uq_provider_user (provider, provider_user_id),
                               KEY idx_user_id (user_id),
                               CONSTRAINT fk_user_identity_user
                                   FOREIGN KEY (user_id) REFERENCES users(user_id)
                                       ON DELETE CASCADE
);

CREATE TABLE email_verification_token (
                                          email_verification_token_id CHAR(36) NOT NULL,
                                          user_id CHAR(36) NOT NULL,
                                          email VARCHAR(320) NOT NULL,
                                          purpose VARCHAR(100) NOT NULL COMMENT 'SIGN_UP | FIND_PASSWORD',
                                          token_hash VARCHAR(255) NOT NULL,
                                          expires_at DATETIME NOT NULL,
                                          used_at DATETIME NULL,
                                          created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                          PRIMARY KEY (email_verification_token_id),
                                          KEY idx_evt_user_id (user_id),
                                          UNIQUE KEY uq_evt_token_hash (token_hash),
                                          CONSTRAINT fk_evt_user
                                              FOREIGN KEY (user_id) REFERENCES users(user_id)
                                                  ON DELETE CASCADE
);