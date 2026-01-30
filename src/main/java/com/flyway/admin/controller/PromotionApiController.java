package com.flyway.admin.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.flyway.admin.dto.PromotionDto;
import com.flyway.admin.service.AdminPromotionService;
import com.flyway.template.common.ApiResponse;
import com.flyway.template.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/public/promotions")
@RequiredArgsConstructor
public class PromotionApiController {

	private final AdminPromotionService promotionService;

	/**
	 * 활성화된 특가 항공권 목록 조회
	 * - 메인페이지에서 사용
	 * - 출발 시간이 현재 이후인 항공편만 조회
	 * - displayOrder 순으로 정렬
	 */
	@GetMapping
	public ApiResponse<List<PromotionDto>> getActivePromotions(
		@RequestParam(defaultValue = "6") int limit) {
		try {
			int safeLimit = Math.max(1, Math.min(limit, 20));
			List<PromotionDto> promotions = promotionService.getActivePromotions(safeLimit);
			return ApiResponse.success(promotions);
		} catch (Exception e) {
			log.error("Failed to get active promotions", e);
			return ApiResponse.error(ErrorCode.PROMO_LIST_FETCH_FAILED.getCode(),
				ErrorCode.PROMO_LIST_FETCH_FAILED.getMessage());
		}
	}

	/**
	 * 특정 특가 항공권 상세 조회
	 * - 예약 페이지 진입 시 사용
	 */
	@GetMapping("/{id}")
	public ApiResponse<PromotionDto> getPromotionById(@PathVariable String id) {
		try {
			PromotionDto promotion = promotionService.getPromotionById(id);
			if (promotion == null || !"Y".equals(promotion.getIsActive())) {
				return ApiResponse.error(ErrorCode.PROMO_NOT_FOUND.getCode(), ErrorCode.PROMO_NOT_FOUND.getMessage());
			}
			return ApiResponse.success(promotion);
		} catch (Exception e) {
			log.error("Failed to get promotion by id: {}", id, e);
			return ApiResponse.error(ErrorCode.PROMO_LIST_FETCH_FAILED.getCode(),
				ErrorCode.PROMO_LIST_FETCH_FAILED.getMessage());
		}
	}

}
