package com.flyway.admin.mapper;

import java.time.LocalDateTime;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.flyway.admin.domain.Admin;

/**
 * 관리자 MyBatis Mapper 인터페이스
 */
@Mapper
public interface AdminMapper {

	/**
	 * 이메일로 관리자 조회
	 */
	Admin findByEmail(@Param("email") String email);

	/**
	 * ID로 관리자 조회
	 */
	Admin findById(@Param("adminId") String adminId);

	/**
	 * 로그인 정보 업데이트 (IP, 로그인 시간)
	 */
	void updateLoginInfo(
		@Param("adminId") String adminId,
		@Param("ipAddress") String ipAddress,
		@Param("loginAt")LocalDateTime loginAt
	);

	/**
	 * 계정 잠금 (30분)
	 */
	void lockAccount(
		@Param("adminId") String adminId,
		@Param("lockedUntil") LocalDateTime lockedUntil
	);

	/**
	 * 로그인 실패 횟수 초기화 및 잠금 해제
	 */
	void resetFailedCount(@Param("adminId") String adminId);

	/**
	 * 관리자 생성
	 */
	void insert(Admin admin);

	/**
	 * 관리자 정보 수정
	 */
	void update(Admin admin);

	/**
	 * 로그인 실패 처리 (Atomic)
	 * 실패 횟수 증가 + 5회 이상 시 자동 잠금
	 */
	void handleLoginFailureAtomic(
			@Param("adminId") String adminId,
			@Param("lockedUntil") LocalDateTime lockedUntil
	);
}
