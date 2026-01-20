package com.flyway.admin.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.flyway.admin.domain.VisitorLog;

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
	long existsTodayBySessionId(@Param("sessionId") String sessionId);
}
