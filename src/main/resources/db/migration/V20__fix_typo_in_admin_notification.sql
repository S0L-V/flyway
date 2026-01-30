ALTER TABLE admin_notification CHANGE COLUMN related_reource_id related_resource_id CHAR(36) NULL;

-- bulk_upload_job 테이블 삭제 (FK 제약조건 먼저 삭제)
ALTER TABLE bulk_upload_job DROP FOREIGN KEY FK_admin_TO_bulk_upload_job_1;
DROP TABLE IF EXISTS bulk_upload_job;