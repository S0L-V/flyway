package com.flyway.template.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * API 공통 응답 형식
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

	private boolean success;
	private T data;
	private String message;
	private String errorCode;

	/**
	 * 성공 응답 (데이터만)
	 */
	public static <T> ApiResponse<T> success(T data) {
		return new ApiResponse<>(true, data, null, null);
	}

	/**
	 * 성공 응답 (데이터 + 메시지)
	 */
	public static <T> ApiResponse<T> success(T data, String message) {
		return new ApiResponse<>(true, data, message, null);
	}

	/**
	 * 실패 응답
	 */
	public static <T> ApiResponse<T> error(String message, String errorCode) {
		return new ApiResponse<>(false, null, message, errorCode);
	}

}
