package com.flyway.admin.service;

import java.util.List;

import com.flyway.admin.dto.PromotionDto;

public interface AdminPromotionService {

	/**
	 * 특가 항공권 목록 조회
	 */
	List<PromotionDto> getPromotionList(int page, int size, String isActive, String searchKeyword);

	/**
	 * 특가 항공권 총 개수 조회
	 */
	int getPromotionCount(String isActive, String searchKeyword);

	/**
	 * ID로 특정 특가 항공권 조회
	 */
	PromotionDto getPromotionById(String promotionId);

	/**
	 * 특가 항공권 생성
	 */
	String createPromotion(PromotionDto promotion, String adminId);

	/**
	 * 특가 항공권 수정
	 */
	boolean updatePromotion(PromotionDto promotion);

	/**
	 * 특가 항공권 삭제
	 */
	boolean deletePromotion(String promotionId);

	/**
	 * 특가 항공권 상태 변경
	 */
	String togglePromotionStatus(String promotionId);

	/**
	 * 특가 항공권 표시 순서 변경
	 */
	boolean updateDisplayOrder(String promotionId, int displayOrder);

	/**
	 * 활성화된 특가 항공권 목록 조회 (메인페이지용)
	 */
	List<PromotionDto> getActivePromotions(int limit);

}
