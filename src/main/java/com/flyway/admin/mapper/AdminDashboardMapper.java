package com.flyway.admin.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.flyway.admin.dto.AdminNotificationDto;
import com.flyway.admin.dto.RecentActivityDto;

/**
 * 관리자, 대시보드 MyBatis Mapper
 * 통계, 최근 활동, 알림 조회
 */
@Mapper
public interface AdminDashboardMapper {

	// == 기간별 통계 조회 ==

	/**
	 * 일일 방문자 수 조회
	 * visitor_log 테이블에서 유니크 세션 기준
	 */
	long countDailyVisitors();

	/**
	 * 일일 결제 완료 건수 조회
	 * payment 테이블에서 오늘 PAID 상태
	 */
	long countDailyPayments();

	/**
	 * 일일 취소/환불 건수 조회
	 * payment 테이블에서 오늘 CANCELLED/REFUNDED 상태
	 */
	long countDailyCancellations();

	/**
	 * 일일 매출 조회
	 * payment 테이블에서 오늘 PAID 상태의 총합
	 */
	long sumDailyRevenue();

	// == 실시간/전체 통계 조회 ==

	/**
	 * 대기 중 예약 수 조회
	 * reservation 테이블에서 HELD 상태 (만료되지 않은 것만)
	 */
	long countPendingReservations();

	/**
	 * 총 회원 수 조회
	 * users 테이블 ACTIVE 상태
	 */
	long countTotalUsers();

	/**
	 * 일일 신규 가입자 수 조회
	 * users 테이블에서 오늘 가입한 회원
	 */
	long countDailyNewUsers();

	/**
	 * 운항 예정 항공편 수 조회
	 * flight 테이블에서 출발 시간이 현재 이후
	 */
	long countActiveFlights();

	// == 최근 활동 조회 ==

	/**
	 * 최근 활동 목록 조회
	 * 예약/결제/환불 내역을 UNION하여 최신순 정렬
	 * @param limit 조회 건수 (기본 10)
	 */
	List<RecentActivityDto> selectRecentActivities(@Param("limit") int limit);

	// == 알림 조회 ==

	/**
	 * 읽지 않은 알림 수 조회
	 * @param adminId 관리자 ID (null이면 전체 알림 포함)
	 */
	long countUnreadNotifications(@Param("adminId") String adminId);

	/**
	 * 알림 목록 조회
	 * @param adminId 관리자 ID (null이면 전체 알림 포함)
	 * @param limit 조회 건수 (기본 10)
	 */
	List<AdminNotificationDto> selectNotifications(@Param("adminId") String adminId, @Param("limit") int limit);

	/**
	 * 알림 읽음 처리
	 * @param notificationId 알림 ID
	 * @param adminId 관리자 ID (권한 체크용)
	 */
	int markNotificationAsRead(@Param("notificationId") String notificationId, @Param("adminId") String adminId);

	/**
	 * 모든 알림 읽음 처리
	 * @param adminId 관리자 ID
	 */
	int markAllNotificationsAsRead(@Param("adminId") String adminId);

	// == 차트용 통계 ==

	/**
	 * 시간대별 예약 분포 조회 (최근 N일)
	 * @param days 조회 기간 (일)
	 * @return 시간대(0-23)별 예약 건수 리스트
	 */
	List<java.util.Map<String, Object>> selectHourlyReservationDistribution(@Param("days") int days);

}
