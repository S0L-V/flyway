package com.flyway.admin.repository;

import com.flyway.admin.domain.Admin;

/**
 * 관리자 Repository 저장소
 * Mapper를 래핑하여 비즈니스 로직 추가
 */
public interface AdminRepository {

	/**
	 * 이메일로 관리자 조회
	 */
	Admin findByEmail(String email);

	/**
	 * ID로 관리자 조회
	 */
	Admin findById(String adminId);

	/**
	 * 로그인 정보 업데이트
	 */
	void updateLoginInfo(String adminId, String ipAddress);

	/**
	 * 로그인 실패 처리
	 * - 실패 횟수 증가
	 * - 5회 이상 시 계정 잠금 (30분)
	 */
	void handleLoginFailure(String adminId);

	/**
	 * 로그인 성공 처리
	 * - 실패 횟수 초기화
	 * - 잠금 해제
	 */
	void handleLoginSuccess(String adminId);

	/**
	 * 관리자 생성
	 */
	void save(Admin admin);

	/**
	 * 관리자 정보 수정
	 */
	void update(Admin admin);
}
