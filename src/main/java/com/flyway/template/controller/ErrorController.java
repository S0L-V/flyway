package com.flyway.template.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

@Slf4j
@Controller
@RequestMapping("/error")
public class ErrorController {

	@GetMapping
	public String handleError(HttpServletRequest request) {
		Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
		String requestUri = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);

		if (status != null) {
			int statusCode = Integer.parseInt(status.toString());

			// 운영 환경 로깅
			if (statusCode == 404) {
				log.warn("[404] 페이지 없음: {}", requestUri);
				return "error/404";
			} else if (statusCode == 403) {
				log.warn("[403] 접근 거부: {}", requestUri);
				return "error/403";
			} else if (statusCode >= 500) {
				Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
				log.error("[{}] 서버 오류: {} - {}", statusCode, requestUri, exception);
				return "error/500";
			}
		}

		return "error/500";
	}

	@GetMapping("/403")
	public String forbidden() {
		return "error/403";
	}

	@GetMapping("/404")
	public String notFound() {
		return "error/404";
	}

	@GetMapping("/500")
	public String serverError() {
		return "error/500";
	}
}
