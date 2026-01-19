-- 1) user_id FK 제거
ALTER TABLE email_verification_token
    DROP FOREIGN KEY fk_evt_user;

-- 2) user_id 인덱스 제거
ALTER TABLE email_verification_token
    DROP INDEX idx_evt_user_id;

-- 3) user_id 컬럼 제거
ALTER TABLE email_verification_token
    DROP COLUMN user_id;

ALTER TABLE flight_seat
    ADD UNIQUE KEY uk_flight_seat (flight_id, aircraft_seat_id);

ALTER TABLE flight
    ADD COLUMN duration_minutes INT COMMENT '비행 시간(분)';