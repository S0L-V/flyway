-- 1) signup_attempt 생성
CREATE TABLE signup_attempt (
                                attempt_id   CHAR(36)      NOT NULL,
                                email        VARCHAR(320)  NOT NULL,
                                status       VARCHAR(20)   NOT NULL COMMENT 'PENDING | VERIFIED | CONSUMED | EXPIRED',
                                expires_at   DATETIME      NOT NULL,
                                verified_at  DATETIME      NULL,
                                consumed_at  DATETIME      NULL,
                                created_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                PRIMARY KEY (attempt_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_signup_attempt_email_created
    ON signup_attempt (email, created_at);

CREATE INDEX idx_signup_attempt_status_expires
    ON signup_attempt (status, expires_at);

-- 2) email_verification_token에 attempt_id 추가
ALTER TABLE email_verification_token
    ADD COLUMN attempt_id CHAR(36) NULL AFTER created_at;

CREATE INDEX idx_evt_attempt_id
    ON email_verification_token (attempt_id);
