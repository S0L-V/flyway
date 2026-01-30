ALTER TABLE admin_notification CHANGE COLUMN related_reource_id related_resource_id CHAR(36) NULL;

DROP TABLE IF EXISTS bulk_upload_job;