package com.flyway.admin.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.flyway.admin.dto.PromotionDto;
import com.flyway.admin.service.AdminPromotionService;
import com.flyway.template.common.ApiResponse;
import com.flyway.template.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/admin/promotions")
@RequiredArgsConstructor
@Slf4j
public class AdminPromotionController {

	private final AdminPromotionService promotionService;

	/**
	 * 특가 항공편 관리 페이지 렌더링
	 */
	@GetMapping
	public String promotionListPage() {
		return "admin/promotions";
	}

	/**
	 * 특가 항공권 목록 API (페이징, 필터링)
	 * GET /admin/promotions/api/list
	 */
	@GetMapping("/api/list")
	@ResponseBody
	public ApiResponse<Map<String, Object>> getPromotionList(
		@RequestParam(defaultValue = "1") int page,
		@RequestParam(defaultValue = "10") int size,
		@RequestParam(required = false) String isActive,
		@RequestParam(required = false) String searchKeyword
	) {
		try {
			List<PromotionDto> list = promotionService.getPromotionList(page, size, isActive, searchKeyword);
			int totalCount = promotionService.getPromotionCount(isActive, searchKeyword);

			Map<String, Object> responseData = new HashMap<>();
			responseData.put("list", list);
			responseData.put("totalCount", totalCount);
			responseData.put("currentPage", page);
			responseData.put("pageSize", size);
			responseData.put("totalPages", (int)Math.ceil((double)totalCount / size));

			return ApiResponse.success(responseData);
		} catch (Exception e) {
			log.error("Failed to get promotion list", e);
			return ApiResponse.error(ErrorCode.PROMO_LIST_FETCH_FAILED.getCode(), ErrorCode.PROMO_LIST_FETCH_FAILED.getMessage());
		}
	}

	/**
	 * ID로 특정 특가 항공권 조회 API
	 * GET /admin/promotions/api/{id}
	 * @param id
	 * @return
	 */
	@GetMapping("/api/{id}")
	@ResponseBody
	public ApiResponse<PromotionDto> getPromotionById(@PathVariable String id) {
		PromotionDto promotion = promotionService.getPromotionById(id);
		if (promotion == null) {
			return ApiResponse.error(ErrorCode.PROMO_NOT_FOUND.getCode(), ErrorCode.PROMO_NOT_FOUND.getMessage());
		}
		return ApiResponse.success(promotion);
	}

	/**
	 * 특가 항공권 생성 API
	 * POST /admin/promotions/api
	 */
	@PostMapping("/api")
	@ResponseBody
	public ApiResponse<String> createPromotion(@RequestBody PromotionDto promotion, HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("adminId") == null) {
			return ApiResponse.error(ErrorCode.UNAUTHORIZED.getCode(), ErrorCode.UNAUTHORIZED.getMessage());
		}
		String adminId = (String)session.getAttribute("adminId");

		String promotionId = promotionService.createPromotion(promotion, adminId);
		if (promotionId == null) {
			return ApiResponse.error(ErrorCode.PROMO_CREATION_FAILED.getCode(),
				ErrorCode.PROMO_CREATION_FAILED.getMessage());
		}
		return ApiResponse.success(promotionId, "프로모션이 생성되었습니다.");
	}

	/**
	 * 특가 항공권 수정 API
	 * PUT /admin/promotions/api/{id}
	 */
	@PutMapping("/api/{id}")
	@ResponseBody
	public ApiResponse<Void> updatePromotion(@PathVariable String id, @RequestBody PromotionDto promotionDto) {
		promotionDto.setPromotionId(id);
		boolean success = promotionService.updatePromotion(promotionDto);
		if (!success) {
			return ApiResponse.error(ErrorCode.PROMO_UPDATE_FAILED.getCode(),
				ErrorCode.PROMO_UPDATE_FAILED.getMessage());
		}
		return ApiResponse.success(null, "프로모션이 수정되었습니다.");
	}

	/**
	 * 특가 항공권 삭제 API
	 * DELETE /admin/promotions/api/{id}
	 */
	@DeleteMapping("/api/{id}")
	@ResponseBody
	public ApiResponse<Void> deletePromotion(@PathVariable String id) {
		boolean success = promotionService.deletePromotion(id);
		if (!success) {
			return ApiResponse.error(ErrorCode.PROMO_DELETE_FAILED.getCode(),
				ErrorCode.PROMO_DELETE_FAILED.getMessage());
		}
		return ApiResponse.success(null, "프로모션이 삭제되었습니다.");
	}

	/**
	 * 특가 항공권 활성화 상태 토글 API
	 */
	@PostMapping("/api/{id}/toggle")
	@ResponseBody
	public ApiResponse<String> togglePromotionStatus(@PathVariable String id) {
		String newStatus = promotionService.togglePromotionStatus(id);
		if (newStatus == null) {
			return ApiResponse.error(ErrorCode.PROMO_TOGGLE_FAILED.getCode(), ErrorCode.PROMO_TOGGLE_FAILED.getMessage());
		}
		return ApiResponse.success(newStatus, "상태가 변경되었습니다.");
	}

	/**
	 * 특가 항공권 표시 순서 변경 API
	 * PUT /admin/promotions/api/{id}/order
	 */
	@PutMapping("/api/{id}/order")
	@ResponseBody
	public ApiResponse<Void> updateDisplayOrder(@PathVariable String id, @RequestBody Map<String, Integer> body) {
		Integer displayOrder = body.get("displayOrder");
		if (displayOrder == null) {
			return ApiResponse.error(ErrorCode.PROMO_INVALID_PARAM.getCode(),
				ErrorCode.PROMO_INVALID_PARAM.getMessage());
		}
		boolean success = promotionService.updateDisplayOrder(id, displayOrder);
		if (!success) {
			return ApiResponse.error(ErrorCode.PROMO_ORDER_UPDATE_FAILED.getCode(),
				ErrorCode.PROMO_UPDATE_FAILED.getMessage());
		}
		return ApiResponse.success(null);
	}
}
