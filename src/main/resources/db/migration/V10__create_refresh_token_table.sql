CREATE TABLE refresh_token
(
    refresh_token_id     CHAR(36) NOT NULL,
    user_id              CHAR(36) NOT NULL,

    -- 해시(원문 X)
    token_hash           CHAR(64) NOT NULL,

    issued_at            DATETIME NOT NULL,
    expires_at           DATETIME NOT NULL,

    -- 로그아웃/강제폐기
    revoked_at           DATETIME NULL,

    -- 회전(정상 사용)된 시점
    rotated_at           DATETIME NULL,

    -- 회전 후 대체된 새 토큰
    replaced_by_token_id CHAR(36) NULL,

    PRIMARY KEY (refresh_token_id),

    -- 동일 토큰 중복 저장 방지
    UNIQUE KEY uq_refresh_token_hash (token_hash),

    -- 조회/정리용 인덱스
    KEY idx_refresh_token_user_expires (user_id, expires_at),
    KEY idx_refresh_token_user_active (user_id, revoked_at, expires_at),

    CONSTRAINT fk_refresh_token_user
        FOREIGN KEY (user_id)
            REFERENCES users (user_id)
            ON DELETE CASCADE
            ON UPDATE RESTRICT,

    CONSTRAINT fk_refresh_token_replaced_by
        FOREIGN KEY (replaced_by_token_id)
            REFERENCES refresh_token (refresh_token_id)
            ON DELETE SET NULL
            ON UPDATE RESTRICT

) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;
