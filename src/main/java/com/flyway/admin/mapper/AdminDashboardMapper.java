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

	// == 통계 조회 ==

	/**
	 * 일일 방문자 수 조회
	 * statistics 테이블에서 오늘 날짜 기준
	 */
	long countDailyVisitors();

	/**
	 * 일일 예약 건수 조회
	 * reservation 테이블에서 오늘 생성된 예약
	 */
	long countDailyReservation();

	/**
	 * 일일 결제 완료 건수 조회
	 * payment 테이블에서 오늘 COMPLETED 상태
	 */
	long countDailyPayments();

	/**
	 * 일일 취소/환불 건수 조회
	 * reservation 테이블에서 오늘 CANCELLED 상태
	 */
	long countDailyCancellations();

	/**
	 * 일일 매출 조회
	 * payment 테이블에서 오늘 COMPLETED 상태의 총합
	 */
	long sumDailyRevenue();

	/**
	 * 총 회원 수 조회
	 * user 테이블 전체 카운트 (is_active = 'Y')
	 */
	long countTotalUsers();

	/**
	 * 운항 중 항공편 수 조회
	 * flight 테이블에서 현재 운항 중인 항공편
	 */
	long countActiveFlights();

	/**
	 * 대기 중 예약 수 조회
	 * reservation 테이블에서 HELD 상태
	 */
	long countPendingReservations();

	/**
	 * 대기 중 결제 수 조회
	 * payment 테이블에서 PENDING 상태
	 */
	long countPendingPayments();

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
}
