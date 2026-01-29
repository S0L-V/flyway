package com.flyway.admin.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.flyway.admin.domain.VisitorLog;
import com.flyway.admin.dto.VisitorDetailDto;

/**
 * 방문자 로그
 * 방문 기록 저장 및 통계 조회
 */
@Mapper
public interface VisitorLogMapper {

	/**
	 * 방문 로그 저장
	 * @param visitorLog 방문 로그 정보
	 */
	void insertVisitorLog(VisitorLog visitorLog);

	/**
	 * 일일 방문자 수 조회 (유니크 세션 기준)
	 */
	long countDailyUniqueVisitors();

	/**
	 * 특정 날짜의 방문자 수 조회
	 * @param date (yyyy-MM-dd)
	 * @return 해당 날짜의 유니크 세션 수
	 */
	long countUniqueVisitorsByDate(@Param("date") String date);

	/**
	 * 특정 세션이 오늘 이미 기록되어 있는지 확인
	 * @param sessionId
	 * @return 기록 존재 여부 (1: 존재, 0: 미존재)
	 */
	int existsTodayBySessionId(@Param("sessionId") String sessionId);

	/**
	 * 오늘 방문자 상세 목록 조회 (유니크 세션 기준, 최신순)
	 * @param limit 조회 건수
	 * @return 방문자 상세 목록
	 */
	List<VisitorDetailDto> selectTodayVisitors(@Param("limit") int limit);

	/**
	 * 비회원 방문 기록에 user_id 업데이트 (로그인 시)
	 * @param sessionId 세션 ID
	 * @param userId 로그인한 사용자 ID
	 * @return 업데이트된 행 수
	 */
	int updateUserIdBySessionId(@Param("sessionId") String sessionId, @Param("userId") String userId);

	/**
	 * 비회원 방문 기록에 user_id 업데이트 (로그인 시) - IP 주소 기반
	 * Spring Security 로그인 시 세션이 재생성되므로 IP 기반으로 매칭
	 * @param ipAddress 클라이언트 IP 주소
	 * @param userId
	 * @return
	 */
	int updateUserIdByIpAddress(@Param("ipAddress") String ipAddress, @Param("userId") String userId);
}
