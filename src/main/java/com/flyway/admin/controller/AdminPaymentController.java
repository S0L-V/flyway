package com.flyway.admin.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.flyway.admin.dto.PaymentListDto;
import com.flyway.admin.dto.PaymentStatsDto;
import com.flyway.admin.service.AdminPaymentService;
import com.flyway.template.common.ApiResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/admin/payments")
@RequiredArgsConstructor
@Slf4j
public class AdminPaymentController {

	private final AdminPaymentService adminPaymentService;

	@GetMapping
	public String paymentListPage(Model model) {
		return "admin/payments";
	}

	/**
	 * 결제 통계 API
	 * GET /admin/payments/api/stats
	 */
	@GetMapping("/api/stats")
	@ResponseBody
	public ApiResponse<PaymentStatsDto> getPaymentStats() {
		try {
			PaymentStatsDto stats = adminPaymentService.getPaymentStats();
			return ApiResponse.success(stats);
		} catch (Exception e) {
			log.error("Failed to get payment stats", e);
			return ApiResponse.error("PAY001", "결제 통계 조회 중 오류가 발생했습니다.");
		}
	}

	@GetMapping("/api/list")
	@ResponseBody
	public ApiResponse<Map<String, Object>> getPaymentList(
		@RequestParam(required = false) String status,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size
	) {
		try {
			List<PaymentListDto> list = adminPaymentService.getPaymentList(status, page, size);
			long totalCount = adminPaymentService.countPayments(status);

			Map<String, Object> responseDate = new HashMap<>();
			responseDate.put("list", list);
			responseDate.put("totalCount", totalCount);
			responseDate.put("currentPage", page);
			responseDate.put("pageSize", size);
			responseDate.put("totalPages", (totalCount + size - 1) / size);

			return ApiResponse.success(responseDate);
		} catch (Exception e) {
			log.error("Failed to get payment list", e);
			return ApiResponse.error("PAY002", "결제 내역 조회 중 오류가 발생했습니다.");
		}
	}
}
