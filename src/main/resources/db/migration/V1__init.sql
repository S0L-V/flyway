CREATE TABLE `user_profile` (
	`user_id`	CHAR(36)	NOT NULL	COMMENT 'PK,FK (1:1)',
	`passport_no`	VARCHAR(20)	NOT NULL,
	`country`	VARCHAR(100)	NOT NULL,
	`gender`	CHAR(1)	NOT NULL	COMMENT 'M/F',
	`first_name`	VARCHAR(100)	NOT NULL,
	`last_name`	VARCHAR(100)	NOT NULL
);

CREATE TABLE `admin` (
	`admin_id`	CHAR(36)	NOT NULL,
	`password_hash`	VARCHAR(255)	NOT NULL,
	`admin_name`	VARCHAR(100)	NOT NULL,
	`email`	VARCHAR(320)	NOT NULL,
	`phone`	VARCHAR(20)	NULL,
	`role`	VARCHAR(20)	NOT NULL	DEFAULT 'ADMIN'	COMMENT 'SUPER_ADMIN | ADMIN | VIEWER',
	`is_active`	CHAR(1)	NOT NULL	DEFAULT 'Y'	COMMENT 'Y | N',
	`last_login_at`	DATETIME	NULL,
	`last_login_ip`	VARCHAR(45)	NULL,
	`password_changed_at`	DATETIME	NULL,
	`failed_login_count`	INT	NULL	DEFAULT 0,
	`locked_until`	TIMESTAMP	NULL,
	`created_at`	DATETIME	NULL	DEFAULT CURRENT_TIMESTAMP,
	`created_by`	CHAR(36)	NULL,
	`updated_at`	DATETIME	NULL,
	`updated_by`	CHAR(36)	NULL
);

CREATE TABLE `reservation_segment` (
	`reservation_segment_id`	CHAR(36)	NOT NULL,
	`flight_id`	CHAR(36)	NOT NULL,
	`reservation_id`	CHAR(36)	NOT NULL,
	`segment_order`	INT	NULL,
	`snap_departure_airport`	char(36)	NOT NULL	COMMENT '스냅샷',
	`snap_arrival_airport`	char(36)	NOT NULL	COMMENT '스냅샷',
	`snap_departure_time`	VARCHAR(20)	NOT NULL	COMMENT '스냅샷',
	`snap_arrival_time`	VARCHAR(20)	NOT NULL	COMMENT '스냅샷',
	`snap_flight_number`	VARCHAR(20)	NOT NULL	COMMENT '스냅샷'
);

CREATE TABLE `admin_action_log` (
	`log_id`	CHAR(36)	NOT NULL,
	`admin_id`	CHAR(36)	NOT NULL,
	`action_type`	VARCHAR(50)	NOT NULL	COMMENT 'LOGIN | LOGOUT | APPROVE_REFUND | CANCEL_RESERVATION | VIEW_USER | UPDATE_FLIGHT',
	`resource_type`	VARCHAR(50)	NULL	COMMENT 'REFUND | RESERVATION | USER | FLIGHT',
	`resource_id`	CHAR(36)	NULL,
	`ip_address`	VARCHAR(45)	NULL,
	`user_agent`	VARCHAR(500)	NULL,
	`created_at`	DATETIME	NULL	DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE `flight_seat` (
	`flight_seat_id`	CHAR(36)	NOT NULL,
	`flight_id`	CHAR(36)	NOT NULL,
	`hold_reservation_segment_id`	CHAR(36)	NULL	COMMENT 'nullable',
	`aircraft_seat_id`	CHAR(36)	NOT NULL,
	`seat_status`	VARCHAR(20)	NOT NULL	DEFAULT 'AVAILABLE'	COMMENT 'AVAILABLE | HOLD | BOOKED',
	`hold_expires_at`	DATETIME	NULL,
	`booked_at`	DATETIME	NULL
);

CREATE TABLE `passenger_seat` (
	`passenger_seat_id`	CHAR(36)	NOT NULL,
	`reservation_segment_id`	CHAR(36)	NOT NULL,
	`passenger_id`	CHAR(36)	NOT NULL,
	`flight_seat_id`	CHAR(36)	NOT NULL
);

CREATE TABLE `cabin_class` (
	`cabin_class_code`	VARCHAR(10)	NOT NULL	COMMENT 'ECO/BIZ/FST',
	`cabin_class_name`	VARCHAR(50)	NOT NULL
);

CREATE TABLE `refund_policy` (
	`RF_ID`	CHAR(36)	NOT NULL,
	`request_day`	DATETIME	NULL	DEFAULT CURRENT_TIMESTAMP,
	`rf_type`	VARCHAR(1)	NOT NULL	COMMENT '1,2,3,4,5 90일/60일/30일/15일/3일',
	`cancellation_fee_rate`	DECIMAL(5, 2)	NOT NULL,
	`airline_id`	VARCHAR(20)	NOT NULL
);

CREATE TABLE `bulk_upload_job` (
	`job_id`	CHAR(36)	NOT NULL,
	`admin_id`	CHAR(36)	NULL,
	`upload_type`	VARCHAR(50)	NOT NULL	COMMENT 'FLIGHT | FARE | BAGGAGE | AIRPLANE',
	`file_name`	VARCHAR(500)	NULL,
	`file_path`	VARCHAR(1000)	NULL,
	`total_rows`	INT	NOT NULL	DEFAULT 0,
	`success_count`	INT	NOT NULL	DEFAULT 0,
	`fail_count`	INT	NOT NULL	DEFAULT 0,
	`status`	VARCHAR(20)	NOT NULL	DEFAULT 'PROCESSING'	COMMENT 'PROCESSING | COMPLETED | FAILED',
	`error_log`	VARCHAR(2000)	NULL,
	`started_at`	DATETIME	NOT NULL	DEFAULT CURRENT_TIMESTAMP,
	`completed_at`	DATETIME	NULL
);

CREATE TABLE `airline` (
	`airline_id`	VARCHAR(20)	NOT NULL	COMMENT 'KE',
	`airline_name`	VARCHAR(100)	NOT NULL,
	`country`	VARCHAR(100)	NOT NULL,
	`logo_url`	VARCHAR(500)	NULL
);

CREATE TABLE `aircraft_seat` (
	`airplane_seat_id`	CHAR(36)	NOT NULL,
	`aircraft_id`	CHAR(36)	NOT NULL,
	`cabin_class_code`	VARCHAR(10)	NOT NULL	COMMENT 'ECO/BIZ/FST/PEY...',
	`seat_no`	VARCHAR(10)	NOT NULL	COMMENT 'ex) 12A',
	`col_no`	VARCHAR(1)	NOT NULL	COMMENT '12A의 A',
	`row_no`	INT	NOT NULL	COMMENT '12A의 12'
);

CREATE TABLE `airport` (
	`airport_id`	VARCHAR(20)	NOT NULL	COMMENT 'ICN 이런식',
	`country`	VARCHAR(100)	NOT NULL,
	`city`	VARCHAR(100)	NOT NULL
);

CREATE TABLE `day_type_policy` (
	`day_id`	VARCHAR(1)	NOT NULL	COMMENT '1 ~ 7',
	`day_type`	VARCHAR(20)	NOT NULL	COMMENT 'WEEKDAY(1~4) | WEEKEND (5~7)',
	`created_at`	DATETIME	NOT NULL	DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE `price_history` (
	`price_history_id`	VARCHAR(36)	NOT NULL,
	`flight_id`	CHAR(36)	NOT NULL,
	`cabin_class_code`	VARCHAR(10)	NOT NULL	COMMENT 'ECO | BIZ | FST',
	`current_price`	BIGINT	NOT NULL,
	`calculated_at`	DATETIME	NOT NULL	DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE `passenger` (
	`passenger_id`	CHAR(36)	NOT NULL,
	`reservation_id`	CHAR(36)	NOT NULL,
	`kr_first_name`	VARCHAR(10)	NOT NULL,
	`kr_last_name`	VARCHAR(10)	NOT NULL,
	`first_name`	VARCHAR(100)	NOT NULL,
	`last_name`	VARCHAR(100)	NOT NULL,
	`passport_no`	VARCHAR(20)	NULL,
	`birth`	DATE	NOT NULL,
	`country`	VARCHAR(100)	NULL,
	`gender`	CHAR(1)	NOT NULL	COMMENT 'M/F',
	`email`	VARCHAR(320)	NOT NULL,
	`checked_baggage_applied`	CHAR(1)	NOT NULL	DEFAULT 'N',
	`phone_number`	VARCHAR(20)	NOT NULL,
	`passport_expiry_date`	DATE	NULL,
	`passport_issue_country`	VARCHAR(100)	NULL,
	`passport_status`	VARCHAR(20)	NOT NULL	DEFAULT 'PENDING'	COMMENT 'PENDING  | SUBMIT',
	`passport_submitted_at`	TIMESTAMP	NULL,
	`passport_reminder_sent_at`	TIMESTAMP	NULL	COMMENT '3일전 , 1일전 발송'
);

CREATE TABLE `passenger_service` (
	`ps_id`	CHAR(36)	NOT NULL,
	`passenger_id`	CHAR(36)	NOT NULL,
	`meal_id`	CHAR(36)	NULL,
	`policy_id`	CHAR(36)	NOT NULL,
	`service_type`	VARCHAR(1)	NULL	COMMENT '0 수하물 | 1 기내식',
	`quantity`	INT	NOT NULL	DEFAULT 1,
	`total_price`	BIGINT	NOT NULL,
	`service_details`	VARCHAR(2000)	NULL	COMMENT 'JSON 형태',
	`trip_type`	VARCHAR(2)	NOT NULL	COMMENT 'OW/RT',
	`added_at`	DATETIME	NOT NULL	DEFAULT CURRENT_TIMESTAMP,
	`reservation_segment_id`	CHAR(36)	NOT NULL
);

CREATE TABLE `aircraft` (
	`aircraft_id`	CHAR(36)	NOT NULL,
	`airline_id`	VARCHAR(20)	NOT NULL,
	`aircraft_type`	VARCHAR(100)	NOT NULL,
	`first_class_seats`	INT	NULL	DEFAULT 0,
	`business_class_seats`	INT	NULL	DEFAULT 0,
	`economy_class_seats`	INT	NULL	DEFAULT 0,
	`total_seats`	INT	NULL
);

CREATE TABLE `statistics` (
	`stat_id`	CHAR(36)	NOT NULL,
	`stat_type`	VARCHAR(20)	NOT NULL	COMMENT 'DAILY | WEEKLY | MONTHLY | YEARLY',
	`stat_date`	DATE	NOT NULL,
	`total_reservations`	INT	NULL	DEFAULT 0,
	`confirmed_reservations`	INT	NULL	DEFAULT 0,
	`cancelled_reservations`	INT	NULL	DEFAULT 0,
	`total_revenue`	BIGINT	NULL	DEFAULT 0,
	`average_ticket_price`	BIGINT	NULL	DEFAULT 0,
	`total_refunds`	BIGINT	NULL	DEFAULT 0,
	`refund_count`	INT	NULL	DEFAULT 0,
	`new_users`	INT	NULL	DEFAULT 0,
	`active_users`	INT	NULL	DEFAULT 0,
	`calculated_at`	DATETIME	NULL	DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE `peak_period` (
	`peak_period_id`	CHAR(36)	NOT NULL,
	`segment_scope`	VARCHAR(30)	NOT NULL	COMMENT 'DOMESTIC| US_DEPARTURE | NON_US_DEPARTURE',
	`start_date`	DATE	NOT NULL,
	`end_date`	DATE	NOT NULL,
	`season_type`	VARCHAR(20)	NOT NULL	COMMENT 'PEAK | SEMI_PEAK',
	`created_at`	DATETIME	NOT NULL	DEFAULT CURRENT_TIMESTAMP	COMMENT '정책 생성 날짜'
);

CREATE TABLE `base_fare` (
	`fare_id`	CHAR(36)	NOT NULL,
	`departure_airport`	VARCHAR(20)	NOT NULL,
	`arrival_airport`	VARCHAR(20)	NOT NULL,
	`cabin_class_code`	VARCHAR(10)	NOT NULL	DEFAULT 'ECO'	COMMENT 'ECO | BIZ | FIRST',
	`airline_id`	VARCHAR(20)	NOT NULL,
	`route_type`	VARCHAR(20)	NOT NULL	DEFAULT 'INTERNATIONAL'	COMMENT 'DOMESTIC | INTERNATIONAL',
	`time_band`	VARCHAR(20)	NULL	COMMENT 'GENERAL | PREFERENCE',
	`day_type`	VARCHAR(20)	NULL	COMMENT 'WEEKDAY | WEEKEND',
	`season_type`	VARCHAR(20)	NULL	COMMENT 'PEAK | SEMI_PEAK | NON_PEAK',
	`base_fare`	BIGINT	NOT NULL	DEFAULT 0,
	`created_at`	DATETIME	NOT NULL	DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE `admin_notification` (
	`notification_id`	CHAR(36)	NOT NULL,
	`admin_id`	CHAR(36)	NULL,
	`notification_type`	VARCHAR(50)	NOT NULL	COMMENT 'NEW_RESERVATION | REFUND_REQUEST | PAYMENT_FAILED | SYSTEM_ALERT',
	`title`	VARCHAR(200)	NOT NULL,
	`message`	VARCHAR(1000)	NULL,
	`related_resource_type`	VARCHAR(50)	NULL	COMMENT 'RESERVATION | REFUND | PAYMENT',
	`related_reource_id`	CHAR(36)	NULL,
	`priority`	VARCHAR(20)	NOT NULL	DEFAULT 'NORMAL'	COMMENT 'HIGH | NORMAL | LOW',
	`is_read`	CHAR(1)	NOT NULL	DEFAULT 'N'	COMMENT 'Y  | N',
	`read_at`	DATETIME	NULL,
	`created_at`	DATETIME	NULL	DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE `flight_number_aircraft_map` (
	`airline_id`	VARCHAR(10)	NOT NULL	COMMENT '항공사 코드랑 항공편명 복합키로 pk구성',
	`flight_number`	VARCHAR(10)	NOT NULL	COMMENT 'KE1238 이런 거',
	`aircraft_id`	CHAR(36)	NOT NULL,
	`is_active`	CHAR(1)	NOT NULL	DEFAULT 'Y'	COMMENT '추후 해당 편명의 운항 기종이 바뀔 수 있으므로'
);

CREATE TABLE `class_info` (
	`class_info_id`	CHAR(36)	NOT NULL,
	`flight_info_id`	CHAR(36)	NOT NULL,
	`cabin_class_code`	VARCHAR(10)	NOT NULL
);

CREATE TABLE `meal_option` (
	`meal_id`	CHAR(36)	NOT NULL,
	`meal_name`	VARCHAR(100)	NOT NULL,
	`available_for_class`	VARCHAR(50)	NULL,
	`image_url`	VARCHAR(500)	NULL,
	`is_active`	CHAR(1)	NOT NULL	DEFAULT 'Y',
	`request`	VARCHAR(1)	NOT NULL	COMMENT 'Y N',
	`route_type`	VARCHAR(20)	NOT NULL	COMMENT 'DOMESTIC  | INTERNATIONAL 스냅샷'
);

CREATE TABLE `users` (
	`user_id`	CHAR(36)	NOT NULL,
	`email`	VARCHAR(320)	NOT NULL,
	`password_hash`	VARCHAR(255)	NULL,
	`login_type`	VARCHAR(20)	NOT NULL	COMMENT 'LOCAL | KAKAO | NAVER | GOOGLE | APPLE',
	`status`	VARCHAR(20)	NOT NULL	COMMENT 'ACTIVE | BLOCKED'
);

CREATE TABLE `payment` (
	`payment_id`	CHAR(36)	NOT NULL,
	`reservation_id`	CHAR(36)	NOT NULL,
	`transaction_id`	CHAR(36)	NOT NULL,
	`created_at`	DATETIME	NOT NULL,
	`paid_at`	DATETIME	NOT NULL,
	`amount`	BIGINT	NOT NULL,
	`payment_method`	VARCHAR(20)	NOT NULL	COMMENT 'CARD | BANK_TRANSFER | KAKAO_PAY | NAVER_PAY',
	`status`	VARCHAR(20)	NOT NULL	COMMENT 'PENDING | PAID | FAILED | CANCELLED | REFUNDED'
);

CREATE TABLE `flight_seat_price` (
	`flight_id`	CHAR(36)	NOT NULL,
	`cabin_class_code`	VARCHAR(10)	NOT NULL	COMMENT 'ECO/BIZ/FST',
	`fare_id`	CHAR(36)	NOT NULL,
	`current_price`	BIGINT	NOT NULL	DEFAULT 0,
	`base_price`	BIGINT	NOT NULL	DEFAULT 0,
	`last_updated`	TIMESTAMP	NOT NULL	DEFAULT CURRENT_TIMESTAMP	COMMENT 'ON UPDATE'
);

CREATE TABLE `flight_info` (
	`flight_info_id`	CHAR(36)	NOT NULL,
	`aircraft_id`	CHAR(36)	NOT NULL,
	`flight_id`	CHAR(36)	NOT NULL,
	`first_class_seat`	INT	NULL,
	`business_class_seat`	INT	NULL,
	`economy_class_seat`	INT	NULL
);

CREATE TABLE `baggage_policy` (
	`policy_id`	CHAR(36)	NOT NULL,
	`cabin_class_code`	VARCHAR(10)	NOT NULL	COMMENT 'replaces seat_class',
	`route_type`	VARCHAR(20)	NOT NULL	COMMENT 'DOMESTIC | INTERNATIONAL',
	`free_checked_bags`	INT	NOT NULL,
	`free_checked_weight_kg`	INT	NOT NULL,
	`extra_bag_fee`	BIGINT	NULL,
	`overweight_fee_per_kg`	BIGINT	NULL,
	`max_weight_per_bag_kg`	INT	NULL,
	`max_total_weight_kg`	INT	NULL
);

CREATE TABLE `domestic_time_band_policy` (
	`policy_id`	CHAR(36)	NOT NULL,
	`arrival_airport`	VARCHAR(20)	NOT NULL	COMMENT 'CJU | NON_CJU',
	`band_type`	VARCHAR(20)	NOT NULL	COMMENT 'GENERAL | PREFERENCE',
	`dep_time_from`	DATETIME	NOT NULL,
	`dep_time_to`	DATETIME	NOT NULL,
	`created_at`	DATETIME	NOT NULL	DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE `flight` (
	`flight_id`	CHAR(36)	NOT NULL,
	`departure_airport`	VARCHAR(20)	NOT NULL,
	`arrival_airport`	VARCHAR(20)	NOT NULL,
	`departure_time`	DATETIME	NOT NULL,
	`arrival_time`	DATETIME	NOT NULL,
	`terminal_no`	VARCHAR(20)	NOT NULL,
	`flight_number`	VARCHAR(10)	NULL,
	`route_type`	VARCHAR(20)	NOT NULL	COMMENT 'DOMESTIC | INTERNATIONAL'
);

CREATE TABLE `reservation` (
	`reservation_id`	CHAR(36)	NOT NULL,
	`user_id`	CHAR(36)	NOT NULL,
	`reserved_at`	DATETIME	NOT NULL,
	`status`	VARCHAR(20)	NOT NULL	COMMENT 'HELD | CONFIRMED | EXPIRED',
	`passenger_count`	int	NOT NULL,
	`trip_type`	VARCHAR(1)	NOT NULL	COMMENT '0 OW | 1 RT',
	`expired_at`	DATETIME	NOT NULL	COMMENT '10분'
);

CREATE TABLE `refund` (
	`refund_id`	CHAR(36)	NOT NULL,
	`reservation_id`	CHAR(36)	NOT NULL,
	`payment_id`	CHAR(36)	NOT NULL,
	`RF_ID`	CHAR(36)	NOT NULL,
	`refund_amount`	BIGINT	NOT NULL,
	`net_refund_amount`	BIGINT	NOT NULL,
	`refund_reason`	VARCHAR(500)	NULL,
	`refund_status`	VARCHAR(20)	NULL	DEFAULT 'PENDING'	COMMENT 'PENDING | APPROVED',
	`requested_at`	DATETIME	NULL	DEFAULT CURRENT_TIMESTAMP,
	`requested_by`	CHAR(36)	NULL,
	`processed_at`	DATETIME	NULL
);

CREATE TABLE `email_log` (
	`log_id`	CHAR(36)	NOT NULL,
	`recipient_email`	VARCHAR(320)	NOT NULL,
	`email_type`	VARCHAR(50)	NOT NULL	COMMENT 'BOOKING_CONFIRM | PRICE_ALERT | PASSPORT_REMINDER | REFUND_CONFIRM',
	`related_resource_type`	VARCHAR(50)	NULL	COMMENT 'RESERVATION | REFUND | PASSENGER',
	`related_resource_id`	CHAR(36)	NULL,
	`subject`	VARCHAR(200)	NULL,
	`sent_status`	VARCHAR(20)	NOT NULL	DEFAULT 'SUCCESS'	COMMENT 'SUCCESS | FAILED',
	`sent_at`	DATETIME	NULL	DEFAULT CURRENT_TIMESTAMP,
	`error_message`	VARCHAR(500)	NULL
);

ALTER TABLE `user_profile` ADD CONSTRAINT `PK_USER_PROFILE` PRIMARY KEY (
	`user_id`
);

ALTER TABLE `admin` ADD CONSTRAINT `PK_ADMIN` PRIMARY KEY (
	`admin_id`
);

ALTER TABLE `reservation_segment` ADD CONSTRAINT `PK_RESERVATION_SEGMENT` PRIMARY KEY (
	`reservation_segment_id`
);

ALTER TABLE `admin_action_log` ADD CONSTRAINT `PK_ADMIN_ACTION_LOG` PRIMARY KEY (
	`log_id`
);

ALTER TABLE `flight_seat` ADD CONSTRAINT `PK_FLIGHT_SEAT` PRIMARY KEY (
	`flight_seat_id`
);

ALTER TABLE `passenger_seat` ADD CONSTRAINT `PK_PASSENGER_SEAT` PRIMARY KEY (
	`passenger_seat_id`
);

ALTER TABLE `cabin_class` ADD CONSTRAINT `PK_CABIN_CLASS` PRIMARY KEY (
	`cabin_class_code`
);

ALTER TABLE `refund_policy` ADD CONSTRAINT `PK_REFUND_POLICY` PRIMARY KEY (
	`RF_ID`
);

ALTER TABLE `bulk_upload_job` ADD CONSTRAINT `PK_BULK_UPLOAD_JOB` PRIMARY KEY (
	`job_id`
);

ALTER TABLE `airline` ADD CONSTRAINT `PK_AIRLINE` PRIMARY KEY (
	`airline_id`
);

ALTER TABLE `aircraft_seat` ADD CONSTRAINT `PK_AIRCRAFT_SEAT` PRIMARY KEY (
	`airplane_seat_id`
);

ALTER TABLE `airport` ADD CONSTRAINT `PK_AIRPORT` PRIMARY KEY (
	`airport_id`
);

ALTER TABLE `day_type_policy` ADD CONSTRAINT `PK_DAY_TYPE_POLICY` PRIMARY KEY (
	`day_id`
);

ALTER TABLE `price_history` ADD CONSTRAINT `PK_PRICE_HISTORY` PRIMARY KEY (
	`price_history_id`
);

ALTER TABLE `passenger` ADD CONSTRAINT `PK_PASSENGER` PRIMARY KEY (
	`passenger_id`
);

ALTER TABLE `passenger_service` ADD CONSTRAINT `PK_PASSENGER_SERVICE` PRIMARY KEY (
	`ps_id`
);

ALTER TABLE `aircraft` ADD CONSTRAINT `PK_AIRCRAFT` PRIMARY KEY (
	`aircraft_id`
);

ALTER TABLE `statistics` ADD CONSTRAINT `PK_STATISTICS` PRIMARY KEY (
	`stat_id`
);

ALTER TABLE `peak_period` ADD CONSTRAINT `PK_PEAK_PERIOD` PRIMARY KEY (
	`peak_period_id`
);

ALTER TABLE `base_fare` ADD CONSTRAINT `PK_BASE_FARE` PRIMARY KEY (
	`fare_id`
);

ALTER TABLE `admin_notification` ADD CONSTRAINT `PK_ADMIN_NOTIFICATION` PRIMARY KEY (
	`notification_id`
);

ALTER TABLE `flight_number_aircraft_map` ADD CONSTRAINT `PK_FLIGHT_NUMBER_AIRCRAFT_MAP` PRIMARY KEY (
	`airline_id`,
	`flight_number`
);

ALTER TABLE `class_info` ADD CONSTRAINT `PK_CLASS_INFO` PRIMARY KEY (
	`class_info_id`
);

ALTER TABLE `meal_option` ADD CONSTRAINT `PK_MEAL_OPTION` PRIMARY KEY (
	`meal_id`
);

ALTER TABLE `users` ADD CONSTRAINT `PK_USERS` PRIMARY KEY (
	`user_id`
);

ALTER TABLE `payment` ADD CONSTRAINT `PK_PAYMENT` PRIMARY KEY (
	`payment_id`
);

ALTER TABLE `flight_seat_price` ADD CONSTRAINT `PK_FLIGHT_SEAT_PRICE` PRIMARY KEY (
	`flight_id`,
	`cabin_class_code`
);

ALTER TABLE `flight_info` ADD CONSTRAINT `PK_FLIGHT_INFO` PRIMARY KEY (
	`flight_info_id`
);

ALTER TABLE `baggage_policy` ADD CONSTRAINT `PK_BAGGAGE_POLICY` PRIMARY KEY (
	`policy_id`
);

ALTER TABLE `domestic_time_band_policy` ADD CONSTRAINT `PK_DOMESTIC_TIME_BAND_POLICY` PRIMARY KEY (
	`policy_id`
);

ALTER TABLE `flight` ADD CONSTRAINT `PK_FLIGHT` PRIMARY KEY (
	`flight_id`
);

ALTER TABLE `reservation` ADD CONSTRAINT `PK_RESERVATION` PRIMARY KEY (
	`reservation_id`
);

ALTER TABLE `refund` ADD CONSTRAINT `PK_REFUND` PRIMARY KEY (
	`refund_id`
);

ALTER TABLE `email_log` ADD CONSTRAINT `PK_EMAIL_LOG` PRIMARY KEY (
	`log_id`
);

ALTER TABLE `user_profile` ADD CONSTRAINT `FK_users_TO_user_profile_1` FOREIGN KEY (
	`user_id`
)
REFERENCES `users` (
	`user_id`
);

ALTER TABLE `admin` ADD CONSTRAINT `FK_admin_TO_admin_1` FOREIGN KEY (
	`created_by`
)
REFERENCES `admin` (
	`admin_id`
);

ALTER TABLE `admin` ADD CONSTRAINT `FK_admin_TO_admin_2` FOREIGN KEY (
	`updated_by`
)
REFERENCES `admin` (
	`admin_id`
);

ALTER TABLE `reservation_segment` ADD CONSTRAINT `FK_flight_TO_reservation_segment_1` FOREIGN KEY (
	`flight_id`
)
REFERENCES `flight` (
	`flight_id`
);

ALTER TABLE `reservation_segment` ADD CONSTRAINT `FK_reservation_TO_reservation_segment_1` FOREIGN KEY (
	`reservation_id`
)
REFERENCES `reservation` (
	`reservation_id`
);

ALTER TABLE `admin_action_log` ADD CONSTRAINT `FK_admin_TO_admin_action_log_1` FOREIGN KEY (
	`admin_id`
)
REFERENCES `admin` (
	`admin_id`
);

ALTER TABLE `flight_seat` ADD CONSTRAINT `FK_flight_TO_flight_seat_1` FOREIGN KEY (
	`flight_id`
)
REFERENCES `flight` (
	`flight_id`
);

ALTER TABLE `flight_seat` ADD CONSTRAINT `FK_reservation_segment_TO_flight_seat_1` FOREIGN KEY (
	`hold_reservation_segment_id`
)
REFERENCES `reservation_segment` (
	`reservation_segment_id`
);

ALTER TABLE `flight_seat` ADD CONSTRAINT `FK_aircraft_seat_TO_flight_seat_1` FOREIGN KEY (
	`aircraft_seat_id`
)
REFERENCES `aircraft_seat` (
	`airplane_seat_id`
);

ALTER TABLE `passenger_seat` ADD CONSTRAINT `FK_reservation_segment_TO_passenger_seat_1` FOREIGN KEY (
	`reservation_segment_id`
)
REFERENCES `reservation_segment` (
	`reservation_segment_id`
);

ALTER TABLE `passenger_seat` ADD CONSTRAINT `FK_passenger_TO_passenger_seat_1` FOREIGN KEY (
	`passenger_id`
)
REFERENCES `passenger` (
	`passenger_id`
);

ALTER TABLE `passenger_seat` ADD CONSTRAINT `FK_flight_seat_TO_passenger_seat_1` FOREIGN KEY (
	`flight_seat_id`
)
REFERENCES `flight_seat` (
	`flight_seat_id`
);

ALTER TABLE `refund_policy` ADD CONSTRAINT `FK_airline_TO_refund_policy_1` FOREIGN KEY (
	`airline_id`
)
REFERENCES `airline` (
	`airline_id`
);

ALTER TABLE `bulk_upload_job` ADD CONSTRAINT `FK_admin_TO_bulk_upload_job_1` FOREIGN KEY (
	`admin_id`
)
REFERENCES `admin` (
	`admin_id`
);

ALTER TABLE `aircraft_seat` ADD CONSTRAINT `FK_aircraft_TO_aircraft_seat_1` FOREIGN KEY (
	`aircraft_id`
)
REFERENCES `aircraft` (
	`aircraft_id`
);

ALTER TABLE `aircraft_seat` ADD CONSTRAINT `FK_cabin_class_TO_aircraft_seat_1` FOREIGN KEY (
	`cabin_class_code`
)
REFERENCES `cabin_class` (
	`cabin_class_code`
);

ALTER TABLE `price_history` ADD CONSTRAINT `FK_flight_TO_price_history_1` FOREIGN KEY (
	`flight_id`
)
REFERENCES `flight` (
	`flight_id`
);

ALTER TABLE `price_history` ADD CONSTRAINT `FK_cabin_class_TO_price_history_1` FOREIGN KEY (
	`cabin_class_code`
)
REFERENCES `cabin_class` (
	`cabin_class_code`
);

ALTER TABLE `passenger` ADD CONSTRAINT `FK_reservation_TO_passenger_1` FOREIGN KEY (
	`reservation_id`
)
REFERENCES `reservation` (
	`reservation_id`
);

ALTER TABLE `passenger_service` ADD CONSTRAINT `FK_passenger_TO_passenger_service_1` FOREIGN KEY (
	`passenger_id`
)
REFERENCES `passenger` (
	`passenger_id`
);

ALTER TABLE `passenger_service` ADD CONSTRAINT `FK_meal_option_TO_passenger_service_1` FOREIGN KEY (
	`meal_id`
)
REFERENCES `meal_option` (
	`meal_id`
);

ALTER TABLE `passenger_service` ADD CONSTRAINT `FK_baggage_policy_TO_passenger_service_1` FOREIGN KEY (
	`policy_id`
)
REFERENCES `baggage_policy` (
	`policy_id`
);

ALTER TABLE `passenger_service` ADD CONSTRAINT `FK_reservation_segment_TO_passenger_service_1` FOREIGN KEY (
	`reservation_segment_id`
)
REFERENCES `reservation_segment` (
	`reservation_segment_id`
);

ALTER TABLE `aircraft` ADD CONSTRAINT `FK_airline_TO_aircraft_1` FOREIGN KEY (
	`airline_id`
)
REFERENCES `airline` (
	`airline_id`
);

ALTER TABLE `base_fare` ADD CONSTRAINT `FK_airport_TO_base_fare_1` FOREIGN KEY (
	`departure_airport`
)
REFERENCES `airport` (
	`airport_id`
);

ALTER TABLE `base_fare` ADD CONSTRAINT `FK_airport_TO_base_fare_2` FOREIGN KEY (
	`arrival_airport`
)
REFERENCES `airport` (
	`airport_id`
);

ALTER TABLE `base_fare` ADD CONSTRAINT `FK_cabin_class_TO_base_fare_1` FOREIGN KEY (
	`cabin_class_code`
)
REFERENCES `cabin_class` (
	`cabin_class_code`
);

ALTER TABLE `base_fare` ADD CONSTRAINT `FK_airline_TO_base_fare_1` FOREIGN KEY (
	`airline_id`
)
REFERENCES `airline` (
	`airline_id`
);

ALTER TABLE `admin_notification` ADD CONSTRAINT `FK_admin_TO_admin_notification_1` FOREIGN KEY (
	`admin_id`
)
REFERENCES `admin` (
	`admin_id`
);

ALTER TABLE `flight_number_aircraft_map` ADD CONSTRAINT `FK_aircraft_TO_flight_number_aircraft_map_1` FOREIGN KEY (
	`aircraft_id`
)
REFERENCES `aircraft` (
	`aircraft_id`
);

ALTER TABLE `class_info` ADD CONSTRAINT `FK_flight_info_TO_class_info_1` FOREIGN KEY (
	`flight_info_id`
)
REFERENCES `flight_info` (
	`flight_info_id`
);

ALTER TABLE `class_info` ADD CONSTRAINT `FK_cabin_class_TO_class_info_1` FOREIGN KEY (
	`cabin_class_code`
)
REFERENCES `cabin_class` (
	`cabin_class_code`
);

ALTER TABLE `payment` ADD CONSTRAINT `FK_reservation_TO_payment_1` FOREIGN KEY (
	`reservation_id`
)
REFERENCES `reservation` (
	`reservation_id`
);

ALTER TABLE `flight_seat_price` ADD CONSTRAINT `FK_flight_TO_flight_seat_price_1` FOREIGN KEY (
	`flight_id`
)
REFERENCES `flight` (
	`flight_id`
);

ALTER TABLE `flight_seat_price` ADD CONSTRAINT `FK_cabin_class_TO_flight_seat_price_1` FOREIGN KEY (
	`cabin_class_code`
)
REFERENCES `cabin_class` (
	`cabin_class_code`
);

ALTER TABLE `flight_seat_price` ADD CONSTRAINT `FK_base_fare_TO_flight_seat_price_1` FOREIGN KEY (
	`fare_id`
)
REFERENCES `base_fare` (
	`fare_id`
);

ALTER TABLE `flight_info` ADD CONSTRAINT `FK_aircraft_TO_flight_info_1` FOREIGN KEY (
	`aircraft_id`
)
REFERENCES `aircraft` (
	`aircraft_id`
);

ALTER TABLE `flight_info` ADD CONSTRAINT `FK_flight_TO_flight_info_1` FOREIGN KEY (
	`flight_id`
)
REFERENCES `flight` (
	`flight_id`
);

ALTER TABLE `baggage_policy` ADD CONSTRAINT `FK_cabin_class_TO_baggage_policy_1` FOREIGN KEY (
	`cabin_class_code`
)
REFERENCES `cabin_class` (
	`cabin_class_code`
);

ALTER TABLE `flight` ADD CONSTRAINT `FK_airport_TO_flight_1` FOREIGN KEY (
	`departure_airport`
)
REFERENCES `airport` (
	`airport_id`
);

ALTER TABLE `flight` ADD CONSTRAINT `FK_airport_TO_flight_2` FOREIGN KEY (
	`arrival_airport`
)
REFERENCES `airport` (
	`airport_id`
);

ALTER TABLE `reservation` ADD CONSTRAINT `FK_users_TO_reservation_1` FOREIGN KEY (
	`user_id`
)
REFERENCES `users` (
	`user_id`
);

ALTER TABLE `refund` ADD CONSTRAINT `FK_reservation_TO_refund_1` FOREIGN KEY (
	`reservation_id`
)
REFERENCES `reservation` (
	`reservation_id`
);

ALTER TABLE `refund` ADD CONSTRAINT `FK_payment_TO_refund_1` FOREIGN KEY (
	`payment_id`
)
REFERENCES `payment` (
	`payment_id`
);

ALTER TABLE `refund` ADD CONSTRAINT `FK_refund_policy_TO_refund_1` FOREIGN KEY (
	`RF_ID`
)
REFERENCES `refund_policy` (
	`RF_ID`
);

