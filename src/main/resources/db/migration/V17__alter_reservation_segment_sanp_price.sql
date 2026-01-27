ALTER TABLE reservation_segment
    ADD COLUMN snap_price BIGINT NOT NULL DEFAULT 0
    COMMENT '1인당 예약 시점 가격';