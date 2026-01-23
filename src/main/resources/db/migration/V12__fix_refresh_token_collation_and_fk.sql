-- V12__fix_refresh_token_collation_and_fk.sql

-- 테이블/컬럼 collation을 users와 동일하게 맞춤
ALTER TABLE refresh_token
    CONVERT TO CHARACTER SET utf8mb4
        COLLATE utf8mb4_unicode_ci;

ALTER TABLE refresh_token
    ADD CONSTRAINT fk_refresh_token_user
        FOREIGN KEY (user_id)
            REFERENCES users (user_id)
            ON DELETE CASCADE
            ON UPDATE RESTRICT;

ALTER TABLE refresh_token
    ADD CONSTRAINT fk_refresh_token_replaced_by
        FOREIGN KEY (replaced_by_token_id)
            REFERENCES refresh_token (refresh_token_id)
            ON DELETE SET NULL
            ON UPDATE RESTRICT;
