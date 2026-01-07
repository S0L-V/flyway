package com.flyway.template.exception;

import com.flyway.template.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 전역 예외 처리
 * 모든 예외를 일괄 처리하여 일관된 응답 형식을 보장
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	/**
	 * 비즈니스 예외 처리
	 * Service 계층에서 throw new BusinessException() 발생 시 처리
	 */
	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
		log.error("BusinessException: [{}] {}", e.getErrorCode().getCode(), e.getMessage());

		ErrorCode errorCode = e.getErrorCode();
		ApiResponse<Void> response = ApiResponse.error(
			errorCode.getCode(),
			e.getMessage()
		);

		return ResponseEntity
			.status(errorCode.getStatus())
			.body(response);
	}

	/**
	 * 일반 예외 처리
	 * 예상하지 못한 모든 예외를 처리합니다.
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
		log.error("Unexpected Exception: ", e);

		ApiResponse<Void> response = ApiResponse.error(
			ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
			ErrorCode.INTERNAL_SERVER_ERROR.getMessage()
		);

		return ResponseEntity
			.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body(response);
	}

	/**
	 * IllegalArgumentException 처리
	 * 잘못된 인자 전달 시 처리
	 */
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
		log.warn("IllegalArgumentException: {}", e.getMessage());

		ApiResponse<Void> response = ApiResponse.error(
			ErrorCode.INVALID_INPUT_VALUE.getCode(),
			e.getMessage()
		);

		return ResponseEntity
			.status(HttpStatus.BAD_REQUEST)
			.body(response);
	}
}
