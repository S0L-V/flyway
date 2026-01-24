-- 가격 변동 사유를 구체적으로 기록
ALTER TABLE price_history
    ADD COLUMN old_price BIGINT NULL COMMENT '변경 직전 가격(없으면 NULL)',
    ADD COLUMN policy_version VARCHAR(30) NOT NULL DEFAULT 'v1' COMMENT '가격 산정 정책/알고리즘 버전',
    ADD COLUMN trigger_ref_id CHAR(36) NULL COMMENT '이벤트/관리자 변경 등 트리거 참조 ID (reservation_id)',
    ADD COLUMN calc_context_json LONGTEXT NULL COMMENT '계산 근거(계수/중간값/캡 적용 등) JSON 문자열';
