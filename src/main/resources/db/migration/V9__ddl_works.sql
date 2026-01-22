-- Processor 조회 속도 향상을 위한 인덱스
CREATE INDEX idx_flight_info_fk
    ON flight_info (flight_id);

-- Batch Reader 조회 속도 향상을 위한 인덱스
CREATE INDEX idx_flight_batch_paging
    ON flight (departure_time, flight_id);

-- 요일 정책 테이블 삭제 (내장함수로 대체)
DROP TABLE IF EXISTS day_type_policy;

ALTER TABLE passenger_service
    MODIFY COLUMN policy_id CHAR(36) NULL;