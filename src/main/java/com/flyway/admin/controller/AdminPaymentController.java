package com.flyway.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.flyway.admin.dto.PaymentListDto;
import com.flyway.admin.repository.AdminPaymentRepository;
import com.flyway.template.common.ApiResponse;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/payments")
@RequiredArgsConstructor
public class AdminPaymentController {


	@GetMapping
	public String paymentListPage(Model model) {
		return "admin/payments";
	}

	@GetMapping("/api/stats")
	@ResponseBody
	public ApiResponse<PaymentListDto> getPaymentStats() {
		try {

		}
	}
}
