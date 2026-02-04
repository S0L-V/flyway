CREATE TABLE sms_verification (
                                  sms_verification_id CHAR(36) NOT NULL,
                                  phone_number VARCHAR(20) NOT NULL,
                                  code VARCHAR(6) NOT NULL,
                                  expires_at DATETIME NOT NULL,
                                  verified_at DATETIME NULL,
                                  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  PRIMARY KEY (sms_verification_id),
                                  INDEX idx_sms_phone (phone_number, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;