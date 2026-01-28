ALTER TABLE payment
    ADD COLUMN order_id VARCHAR(64) NULL COMMENT '주문 ID';

ALTER TABLE payment
    ADD COLUMN payment_key VARCHAR(200) NULL COMMENT '토스 결제 키';

ALTER TABLE payment
    MODIFY COLUMN transaction_id CHAR(36) NULL COMMENT '미사용';

ALTER TABLE payment
    MODIFY COLUMN paid_at DATETIME NULL COMMENT '결제 완료 시간';

CREATE UNIQUE INDEX idx_payment_order_id ON payment(order_id);
CREATE INDEX idx_payment_payment_key ON payment(payment_key);