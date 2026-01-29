package com.flyway.admin.service;

import java.util.List;
import java.util.Map;

import com.flyway.admin.dto.AdminUserDto;

public interface AdminUserService {

	/**
	 * 회원 목록 조회 (페이징, 검색, 필터)
	 */
	List<AdminUserDto> getUserList(String status, String searchKeyword, int page, int size);

	/**
	 * 회원 수 조회
	 */
	int getUserCount(String status, String searchKeyword);

	/**
	 * 회원 상세 조회
	 */
	AdminUserDto getUserById(String userId);

	/**
	 * 회원 상태 변경 (ACTIVE <-> BLOCKED)
	 */
	boolean updateUserStatus(String userId, String status);

	/**
	 * 상태별 회원 통계
	 */
	Map<String, Integer> getUserStats();
}
