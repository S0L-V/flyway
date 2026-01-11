-- =============================================
-- V1__init.sql
-- 초기 DB 스키마 생성
-- =============================================

-- 기본 참조 테이블 먼저 생성
CREATE TABLE airport (
                         airport_id VARCHAR(20) NOT NULL COMMENT 'ICN 같은 공항 코드',
                         country VARCHAR(100) NOT NULL,
                         city VARCHAR(100) NOT NULL,
                         PRIMARY KEY (airport_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='공항 정보';

CREATE TABLE cabin_class (
                             cabin_class_code VARCHAR(10) NOT NULL COMMENT 'ECO/BIZ/FST',
                             cabin_class_name VARCHAR(50) NOT NULL,
                             PRIMARY KEY (cabin_class_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='좌석 등급';

CREATE TABLE airline (
                         airline_id VARCHAR(20) NOT NULL COMMENT 'KE 같은 항공사 코드',
                         airline_name VARCHAR(100) NOT NULL,
                         country VARCHAR(100) NOT NULL,
                         logo_url VARCHAR(500) NULL,
                         PRIMARY KEY (airline_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='항공사 정보';

-- 항공기 정보
CREATE TABLE aircraft (
                          aircraft_id CHAR(36) NOT NULL,
                          airline_code VARCHAR(20) NOT NULL,
                          aircraft_type VARCHAR(100) NOT NULL,
                          first_class_seats INT NULL DEFAULT 0,
                          business_class_seats INT NULL DEFAULT 0,
                          economy_class_seats INT NULL DEFAULT 0,
                          total_seats INT NULL,
                          PRIMARY KEY (aircraft_id),
                          FOREIGN KEY (airline_code) REFERENCES airline(airline_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='항공기 정보';

-- 항공기 좌석
CREATE TABLE aircraft_seat (
                               airplane_seat_id CHAR(36) NOT NULL,
                               aircraft_id CHAR(36) NOT NULL,
                               seat_no VARCHAR(10) NOT NULL COMMENT 'ex) 12A',
                               col_no VARCHAR(1) NOT NULL COMMENT '12A의 A',
                               row_no INT NOT NULL COMMENT '12A의 12',
                               cabin_class_code VARCHAR(10) NOT NULL COMMENT 'ECO/BIZ/FST',
                               PRIMARY KEY (airplane_seat_id),
                               FOREIGN KEY (aircraft_id) REFERENCES aircraft(aircraft_id),
                               FOREIGN KEY (cabin_class_code) REFERENCES cabin_class(cabin_class_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='항공기 좌석 배치';

-- 항공편 번호-기종 매핑
CREATE TABLE flight_number_aircraft_map (
                                            airline_code VARCHAR(10) NOT NULL COMMENT '항공사 코드',
                                            flight_number VARCHAR(10) NOT NULL COMMENT 'KE1238',
                                            is_active CHAR(1) NOT NULL DEFAULT 'Y' COMMENT 'Y or N',
                                            aircraft_id CHAR(36) NOT NULL,
                                            PRIMARY KEY (airline_code, flight_number),
                                            FOREIGN KEY (aircraft_id) REFERENCES aircraft(aircraft_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='항공편-기종 매핑';

-- 항공편
CREATE TABLE flight (
                        flight_id CHAR(36) NOT NULL,
                        departure_airport VARCHAR(20) NOT NULL,
                        arrival_airport VARCHAR(20) NOT NULL,
                        departure_time DATETIME NOT NULL,
                        arrival_time DATETIME NOT NULL,
                        terminal_no VARCHAR(20) NOT NULL,
                        flight_number VARCHAR(10) NULL,
                        route_type VARCHAR(20) NOT NULL COMMENT 'DOMESTIC or INTERNATIONAL',
                        PRIMARY KEY (flight_id),
                        FOREIGN KEY (departure_airport) REFERENCES airport(airport_id),
                        FOREIGN KEY (arrival_airport) REFERENCES airport(airport_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='항공편 정보';

-- 항공편 정보
CREATE TABLE flight_info (
                             flight_info_id CHAR(36) NOT NULL,
                             aircraft_id CHAR(36) NOT NULL,
                             first_class_seat INT NULL,
                             business_class_seat INT NULL,
                             economy_class_seat INT NULL,
                             flight_id CHAR(36) NOT NULL,
                             PRIMARY KEY (flight_info_id),
                             FOREIGN KEY (aircraft_id) REFERENCES aircraft(aircraft_id),
                             FOREIGN KEY (flight_id) REFERENCES flight(flight_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='항공편 상세 정보';

-- 클래스 정보
CREATE TABLE class_info (
                            class_info_id CHAR(36) NOT NULL,
                            flight_info_id CHAR(36) NOT NULL,
                            cabin_class_code VARCHAR(10) NOT NULL,
                            PRIMARY KEY (class_info_id),
                            FOREIGN KEY (flight_info_id) REFERENCES flight_info(flight_info_id),
                            FOREIGN KEY (cabin_class_code) REFERENCES cabin_class(cabin_class_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='클래스 정보';

-- 요금 정책
CREATE TABLE peak_period (
                             peak_period_id CHAR(36) NOT NULL,
                             segment_scope VARCHAR(30) NOT NULL COMMENT 'DOMESTIC or US_DEPARTURE or NON_US_DEPARTURE',
                             start_date DATE NOT NULL,
                             end_date DATE NOT NULL,
                             season_type VARCHAR(20) NOT NULL COMMENT 'PEAK or SEMI_PEAK',
                             created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '정책 생성 날짜',
                             PRIMARY KEY (peak_period_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='성수기 기간';

CREATE TABLE day_type_policy (
                                 policy_id CHAR(36) NOT NULL,
                                 weekday_days VARCHAR(20) NULL,
                                 weekend_days VARCHAR(20) NULL,
                                 created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 PRIMARY KEY (policy_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='요일 타입 정책';

CREATE TABLE domestic_time_band_policy (
                                           policy_id CHAR(36) NOT NULL,
                                           arrival_airport VARCHAR(20) NOT NULL COMMENT 'CJU or NON_CJU',
                                           band_type VARCHAR(20) NOT NULL COMMENT 'GENERAL or PREFERENCE',
                                           dep_time_from DATETIME NOT NULL,
                                           dep_time_to DATETIME NOT NULL,
                                           created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                           PRIMARY KEY (policy_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='국내선 시간대 정책';

CREATE TABLE base_fare (
                           fare_id CHAR(36) NOT NULL,
                           departure_airport VARCHAR(20) NOT NULL,
                           arrival_airport VARCHAR(20) NOT NULL,
                           cabin_class_code VARCHAR(10) NOT NULL DEFAULT 'ECO' COMMENT 'ECO or BIZ or FIRST',
                           airline_code VARCHAR(20) NOT NULL,
                           trip_type VARCHAR(20) NOT NULL DEFAULT 'RT' COMMENT 'OW (one way) or RT (round trip)',
                           route_type VARCHAR(20) NOT NULL DEFAULT 'INTERNATIONAL' COMMENT 'DOMESTIC or INTERNATIONAL',
                           time_band VARCHAR(20) NOT NULL DEFAULT 'NONE' COMMENT 'GENERAL or PREFERENCE or NONE',
                           day_type VARCHAR(20) NOT NULL DEFAULT 'NONE' COMMENT 'WEEKDAY or WEEKEND or NONE',
                           season_type VARCHAR(20) NOT NULL DEFAULT 'NON_PEAK' COMMENT 'PEAK or SEMI_PEAK or NON_PEAK',
                           base_fare BIGINT NOT NULL DEFAULT 0,
                           created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           PRIMARY KEY (fare_id),
                           FOREIGN KEY (departure_airport) REFERENCES airport(airport_id),
                           FOREIGN KEY (arrival_airport) REFERENCES airport(airport_id),
                           FOREIGN KEY (cabin_class_code) REFERENCES cabin_class(cabin_class_code),
                           FOREIGN KEY (airline_code) REFERENCES airline(airline_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='기본 요금';

CREATE TABLE flight_seat_price (
                                   flight_id CHAR(36) NOT NULL,
                                   cabin_class_code VARCHAR(10) NOT NULL COMMENT 'ECO/BIZ/FST',
                                   fare_id CHAR(36) NOT NULL,
                                   current_price BIGINT NOT NULL DEFAULT 0,
                                   base_price BIGINT NOT NULL DEFAULT 0,
                                   last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'ON UPDATE',
                                   PRIMARY KEY (flight_id, cabin_class_code),
                                   FOREIGN KEY (flight_id) REFERENCES flight(flight_id),
                                   FOREIGN KEY (cabin_class_code) REFERENCES cabin_class(cabin_class_code),
                                   FOREIGN KEY (fare_id) REFERENCES base_fare(fare_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='항공편 좌석 가격';

CREATE TABLE price_history (
                               price_history_id CHAR(36) NOT NULL,
                               flight_id CHAR(36) NOT NULL,
                               cabin_class_code VARCHAR(10) NOT NULL COMMENT 'ECO or BIZ or FST',
                               current_price BIGINT NOT NULL,
                               calculated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               PRIMARY KEY (price_history_id),
                               FOREIGN KEY (flight_id) REFERENCES flight(flight_id),
                               FOREIGN KEY (cabin_class_code) REFERENCES cabin_class(cabin_class_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='가격 이력';

-- 수하물 및 기내식
CREATE TABLE baggage_policy (
                                policy_id CHAR(36) NOT NULL,
                                cabin_class_code VARCHAR(10) NOT NULL,
                                route_type VARCHAR(20) NOT NULL COMMENT 'DOMESTIC or INTERNATIONAL',
                                free_checked_bags INT NOT NULL,
                                free_checked_weight_kg INT NOT NULL,
                                extra_bag_fee BIGINT NULL,
                                overweight_fee_per_kg BIGINT NULL,
                                max_weight_per_bag_kg INT NULL,
                                max_total_weight_kg INT NULL,
                                PRIMARY KEY (policy_id),
                                FOREIGN KEY (cabin_class_code) REFERENCES cabin_class(cabin_class_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='수하물 정책';

CREATE TABLE meal_option (
                             meal_id CHAR(36) NOT NULL,
                             meal_name VARCHAR(100) NOT NULL,
                             available_for_class VARCHAR(50) NULL,
                             image_url VARCHAR(500) NULL,
                             is_active CHAR(1) NOT NULL DEFAULT 'Y',
                             request VARCHAR(1) NOT NULL COMMENT 'Y or N',
                             route_type VARCHAR(20) NOT NULL COMMENT 'DOMESTIC or INTERNATIONAL',
                             PRIMARY KEY (meal_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='기내식 옵션';

-- 사용자
CREATE TABLE users (
                       user_id CHAR(36) NOT NULL,
                       email VARCHAR(320) NOT NULL,
                       password_hash VARCHAR(255) NULL,
                       login_type VARCHAR(20) NOT NULL COMMENT 'LOCAL or KAKAO or NAVER or GOOGLE or APPLE',
                       status VARCHAR(20) NOT NULL COMMENT 'ACTIVE or BLOCKED',
                       PRIMARY KEY (user_id),
                       UNIQUE KEY uk_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='사용자';

CREATE TABLE user_profile (
                              user_id CHAR(36) NOT NULL COMMENT 'PK,FK (1:1)',
                              passport_no VARCHAR(20) NOT NULL,
                              country VARCHAR(100) NOT NULL,
                              gender CHAR(1) NOT NULL COMMENT 'M/F',
                              first_name VARCHAR(100) NOT NULL,
                              last_name VARCHAR(100) NOT NULL,
                              PRIMARY KEY (user_id),
                              FOREIGN KEY (user_id) REFERENCES users(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='사용자 프로필';

-- 예약
CREATE TABLE reservation (
                             reservation_id CHAR(36) NOT NULL,
                             user_id CHAR(36) NOT NULL,
                             reserved_at DATETIME NOT NULL,
                             status VARCHAR(20) NOT NULL COMMENT 'HELD or CONFIRMED or EXPIRED',
                             passenger_count INT NOT NULL,
                             trip_type VARCHAR(1) NOT NULL COMMENT '0 OW or 1 RT',
                             expired_at DATETIME NOT NULL COMMENT '10분',
                             PRIMARY KEY (reservation_id),
                             FOREIGN KEY (user_id) REFERENCES users(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='예약';

CREATE TABLE reservation_segment (
                                     reservation_segment_id CHAR(36) NOT NULL,
                                     flight_id CHAR(36) NOT NULL,
                                     reservation_id CHAR(36) NOT NULL,
                                     segment_order INT NULL,
                                     snap_departure_airport CHAR(20) NOT NULL COMMENT '스냅샷',
                                     snap_arrival_airport CHAR(20) NOT NULL COMMENT '스냅샷',
                                     snap_departure_time DATETIME NOT NULL COMMENT '스냅샷',
                                     snap_arrival_time DATETIME NOT NULL COMMENT '스냅샷',
                                     snap_flight_number VARCHAR(20) NOT NULL COMMENT '스냅샷',
                                     PRIMARY KEY (reservation_segment_id),
                                     FOREIGN KEY (flight_id) REFERENCES flight(flight_id),
                                     FOREIGN KEY (reservation_id) REFERENCES reservation(reservation_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='예약 구간';

CREATE TABLE flight_seat (
                             flight_seat_id CHAR(36) NOT NULL,
                             flight_id CHAR(36) NOT NULL,
                             hold_reservation_segment_id CHAR(36) NULL COMMENT 'nullable',
                             aircraft_seat_id CHAR(36) NOT NULL,
                             seat_status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE' COMMENT 'AVAILABLE or HOLD or BOOKED',
                             hold_expires_at DATETIME NULL,
                             booked_at DATETIME NULL,
                             PRIMARY KEY (flight_seat_id),
                             FOREIGN KEY (flight_id) REFERENCES flight(flight_id),
                             FOREIGN KEY (hold_reservation_segment_id) REFERENCES reservation_segment(reservation_segment_id),
                             FOREIGN KEY (aircraft_seat_id) REFERENCES aircraft_seat(airplane_seat_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='항공편 좌석';

CREATE TABLE passenger (
                           passenger_id CHAR(36) NOT NULL,
                           reservation_id CHAR(36) NOT NULL,
                           kr_first_name VARCHAR(10) NOT NULL,
                           kr_last_name VARCHAR(10) NOT NULL,
                           first_name VARCHAR(100) NOT NULL,
                           last_name VARCHAR(100) NOT NULL,
                           passport_no VARCHAR(20) NULL,
                           birth DATE NOT NULL,
                           country VARCHAR(100) NULL,
                           gender CHAR(1) NOT NULL COMMENT 'M/F',
                           email VARCHAR(320) NOT NULL,
                           checked_baggage_applied CHAR(1) NOT NULL DEFAULT 'N',
                           phone_number VARCHAR(20) NOT NULL,
                           passport_expiry_date DATE NULL,
                           passport_issue_country VARCHAR(100) NULL,
                           passport_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING or SUBMIT',
                           passport_submitted_at TIMESTAMP NULL,
                           passport_reminder_sent_at TIMESTAMP NULL COMMENT '3일전, 1일전 발송',
                           PRIMARY KEY (passenger_id),
                           FOREIGN KEY (reservation_id) REFERENCES reservation(reservation_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='승객';

CREATE TABLE passenger_seat (
                                passenger_seat_id CHAR(36) NOT NULL,
                                reservation_segment_id CHAR(36) NOT NULL,
                                passenger_id CHAR(36) NOT NULL,
                                flight_seat_id CHAR(36) NOT NULL,
                                PRIMARY KEY (passenger_seat_id),
                                FOREIGN KEY (reservation_segment_id) REFERENCES reservation_segment(reservation_segment_id),
                                FOREIGN KEY (passenger_id) REFERENCES passenger(passenger_id),
                                FOREIGN KEY (flight_seat_id) REFERENCES flight_seat(flight_seat_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='승객 좌석';

CREATE TABLE passenger_service (
                                   ps_id CHAR(36) NOT NULL,
                                   passenger_id CHAR(36) NOT NULL,
                                   meal_id CHAR(36) NULL,
                                   policy_id CHAR(36) NOT NULL,
                                   service_type VARCHAR(1) NULL COMMENT '0 수하물 or 1 기내식',
                                   quantity INT NOT NULL DEFAULT 1,
                                   total_price BIGINT NOT NULL,
                                   service_details VARCHAR(2000) NULL COMMENT 'JSON 형태',
                                   trip_type VARCHAR(2) NOT NULL COMMENT 'OW/RT',
                                   added_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                   PRIMARY KEY (ps_id),
                                   FOREIGN KEY (passenger_id) REFERENCES passenger(passenger_id),
                                   FOREIGN KEY (meal_id) REFERENCES meal_option(meal_id),
                                   FOREIGN KEY (policy_id) REFERENCES baggage_policy(policy_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='승객 서비스';

-- 결제
CREATE TABLE payment (
                         payment_id CHAR(36) NOT NULL,
                         reservation_id CHAR(36) NOT NULL,
                         transaction_id CHAR(36) NOT NULL,
                         created_at DATETIME NOT NULL,
                         paid_at DATETIME NOT NULL,
                         amount BIGINT NOT NULL,
                         payment_method VARCHAR(20) NOT NULL COMMENT 'CARD or BANK_TRANSFER or KAKAO_PAY or NAVER_PAY',
                         status VARCHAR(20) NOT NULL COMMENT 'PENDING or PAID or FAILED or CANCELLED or REFUNDED',
                         PRIMARY KEY (payment_id),
                         FOREIGN KEY (reservation_id) REFERENCES reservation(reservation_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='결제';

CREATE TABLE REFUND_POLICY (
                               RF_ID CHAR(36) NOT NULL,
                               request_day DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
                               rf_type VARCHAR(1) NOT NULL COMMENT '1,2,3,4,5 90일/60일/30일/15일/3일',
                               cancellation_fee_rate DECIMAL(5, 2) NOT NULL,
                               airline_code VARCHAR(20) NOT NULL,
                               PRIMARY KEY (RF_ID),
                               FOREIGN KEY (airline_code) REFERENCES airline(airline_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='환불 정책';

CREATE TABLE REFUND (
                        refund_id CHAR(36) NOT NULL,
                        reservation_id CHAR(36) NOT NULL,
                        payment_id CHAR(36) NOT NULL,
                        RF_ID CHAR(36) NOT NULL,
                        refund_amount BIGINT NOT NULL,
                        net_refund_amount BIGINT NOT NULL,
                        refund_reason VARCHAR(500) NULL,
                        refund_status VARCHAR(20) NULL DEFAULT 'PENDING' COMMENT 'PENDING or APPROVED',
                        requested_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
                        requested_by CHAR(36) NULL,
                        processed_at DATETIME NULL,
                        PRIMARY KEY (refund_id),
                        FOREIGN KEY (reservation_id) REFERENCES reservation(reservation_id),
                        FOREIGN KEY (payment_id) REFERENCES payment(payment_id),
                        FOREIGN KEY (RF_ID) REFERENCES REFUND_POLICY(RF_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='환불';

-- 관리자 (순환 참조 제거)
CREATE TABLE ADMIN (
                       admin_id CHAR(36) NOT NULL,
                       password_hash VARCHAR(255) NOT NULL,
                       admin_name VARCHAR(100) NOT NULL,
                       email VARCHAR(320) NOT NULL,
                       phone VARCHAR(20) NULL,
                       role VARCHAR(20) NOT NULL DEFAULT 'ADMIN' COMMENT 'SUPER_ADMIN or ADMIN or VIEWER',
                       is_active CHAR(1) NOT NULL DEFAULT 'Y' COMMENT 'Y or N',
                       last_login_at DATETIME NULL,
                       last_login_ip VARCHAR(45) NULL,
                       password_changed_at DATETIME NULL,
                       failed_login_count INT NULL DEFAULT 0,
                       locked_until TIMESTAMP NULL,
                       created_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
                       created_by CHAR(36) NULL,
                       updated_at DATETIME NULL,
                       updated_by CHAR(36) NULL,
                       PRIMARY KEY (admin_id),
                       UNIQUE KEY uk_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='관리자';

CREATE TABLE ADMIN_ACTION_LOG (
                                  log_id CHAR(36) NOT NULL,
                                  admin_id CHAR(36) NOT NULL,
                                  action_type VARCHAR(50) NOT NULL COMMENT 'LOGIN or LOGOUT or APPROVE_REFUND etc',
                                  resource_type VARCHAR(50) NULL COMMENT 'REFUND or RESERVATION or USER or FLIGHT',
                                  resource_id CHAR(36) NULL,
                                  ip_address VARCHAR(45) NULL,
                                  user_agent VARCHAR(500) NULL,
                                  created_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
                                  PRIMARY KEY (log_id),
                                  FOREIGN KEY (admin_id) REFERENCES ADMIN(admin_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='관리자 액션 로그';

CREATE TABLE ADMIN_NOTIFICATION (
                                    notification_id CHAR(36) NOT NULL,
                                    admin_id CHAR(36) NULL,
                                    notification_type VARCHAR(50) NOT NULL COMMENT 'NEW_RESERVATION or REFUND_REQUEST etc',
                                    title VARCHAR(200) NOT NULL,
                                    message VARCHAR(1000) NULL,
                                    related_resource_type VARCHAR(50) NULL COMMENT 'RESERVATION or REFUND or PAYMENT',
                                    related_resource_id CHAR(36) NULL,
                                    priority VARCHAR(20) NOT NULL DEFAULT 'NORMAL' COMMENT 'HIGH or NORMAL or LOW',
                                    is_read CHAR(1) NOT NULL DEFAULT 'N' COMMENT 'Y or N',
                                    read_at DATETIME NULL,
                                    created_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
                                    PRIMARY KEY (notification_id),
                                    FOREIGN KEY (admin_id) REFERENCES ADMIN(admin_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='관리자 알림';

CREATE TABLE STATISTICS (
                            stat_id CHAR(36) NOT NULL,
                            stat_type VARCHAR(20) NOT NULL COMMENT 'DAILY or WEEKLY or MONTHLY or YEARLY',
                            stat_date DATE NOT NULL,
                            total_reservations INT NULL DEFAULT 0,
                            confirmed_reservations INT NULL DEFAULT 0,
                            cancelled_reservations INT NULL DEFAULT 0,
                            total_revenue BIGINT NULL DEFAULT 0,
                            average_ticket_price BIGINT NULL DEFAULT 0,
                            total_refunds BIGINT NULL DEFAULT 0,
                            refund_count INT NULL DEFAULT 0,
                            new_users INT NULL DEFAULT 0,
                            active_users INT NULL DEFAULT 0,
                            calculated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
                            PRIMARY KEY (stat_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='통계';

CREATE TABLE EMAIL_LOG (
                           log_id CHAR(36) NOT NULL,
                           recipient_email VARCHAR(320) NOT NULL,
                           email_type VARCHAR(50) NOT NULL COMMENT 'BOOKING_CONFIRM or PRICE_ALERT etc',
                           related_resource_type VARCHAR(50) NULL COMMENT 'RESERVATION or REFUND or PASSENGER',
                           related_resource_id CHAR(36) NULL,
                           subject VARCHAR(200) NULL,
                           sent_status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS' COMMENT 'SUCCESS or FAILED',
                           sent_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
                           error_message VARCHAR(500) NULL,
                           PRIMARY KEY (log_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='이메일 로그';

CREATE TABLE BULK_UPLOAD_JOB (
                                 job_id CHAR(36) NOT NULL,
                                 admin_id CHAR(36) NULL,
                                 upload_type VARCHAR(50) NOT NULL COMMENT 'FLIGHT or FARE or BAGGAGE or AIRPLANE',
                                 file_name VARCHAR(500) NULL,
                                 file_path VARCHAR(1000) NULL,
                                 total_rows INT NOT NULL DEFAULT 0,
                                 success_count INT NOT NULL DEFAULT 0,
                                 fail_count INT NOT NULL DEFAULT 0,
                                 status VARCHAR(20) NOT NULL DEFAULT 'PROCESSING' COMMENT 'PROCESSING or COMPLETED or FAILED',
                                 error_log VARCHAR(2000) NULL,
                                 started_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 completed_at DATETIME NULL,
                                 PRIMARY KEY (job_id),
                                 FOREIGN KEY (admin_id) REFERENCES ADMIN(admin_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='대량 업로드 작업';
