CREATE INDEX idx_reservation_status_expired
    ON reservation (status, expired_at);