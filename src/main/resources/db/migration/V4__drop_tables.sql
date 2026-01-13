-- 1. FK 제약 비활성화
SET FOREIGN_KEY_CHECKS = 0;

-- 2. 모든 테이블 삭제
DROP TABLE IF EXISTS passenger_seat;
DROP TABLE IF EXISTS class_info;
DROP TABLE IF EXISTS passenger;
DROP TABLE IF EXISTS reservation_segment;
DROP TABLE IF EXISTS bulk_upload_job;
DROP TABLE IF EXISTS payment;
DROP TABLE IF EXISTS airline;
DROP TABLE IF EXISTS cabin_class;
DROP TABLE IF EXISTS airport;
DROP TABLE IF EXISTS baggage_policy;
DROP TABLE IF EXISTS aircraft_seat;
DROP TABLE IF EXISTS domestic_time_band_policy;
DROP TABLE IF EXISTS user_profile;
DROP TABLE IF EXISTS flight_info;
DROP TABLE IF EXISTS price_history;
DROP TABLE IF EXISTS email_log;
DROP TABLE IF EXISTS refund;
DROP TABLE IF EXISTS statistics;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS passenger_service;
DROP TABLE IF EXISTS meal_option;
DROP TABLE IF EXISTS aircraft;
DROP TABLE IF EXISTS flight_seat_price;
DROP TABLE IF EXISTS flight_seat;
DROP TABLE IF EXISTS admin_notification;
DROP TABLE IF EXISTS refund_policy;
DROP TABLE IF EXISTS flight_number_aircraft_map;
DROP TABLE IF EXISTS peak_period;
DROP TABLE IF EXISTS base_fare;
DROP TABLE IF EXISTS admin_action_log;
DROP TABLE IF EXISTS admin;
DROP TABLE IF EXISTS day_type_policy;
DROP TABLE IF EXISTS flight;
DROP TABLE IF EXISTS reservation;

-- 3. FK 제약 다시 활성화
SET FOREIGN_KEY_CHECKS = 1;