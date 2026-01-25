package com.flyway.template.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * API 공통 응답 형식
 *
 * @Param <T> 응답 데이터 확인
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

	private boolean success;
	private T data;
	private String errorCode;
	private String message;

	/**
	 * 성공 응답 (데이터만)
	 * @param data
	 * @return
	 * @param <T>
	 */
	public static <T> ApiResponse<T> success(T data) {
		return new ApiResponse<>(true, data, null, null);
	}

	/**
	 * 성공 응답 (데이터 + 메시지)
	 * @param data
	 * @param message
	 * @return
	 * @param <T>
	 */
	public static <T> ApiResponse<T> success(T data, String message) {
		return new ApiResponse<>(true, data, null, message);
	}

	/**
	 * 실패 응답
	 * @param errorCode
	 * @param message
	 * @return
	 * @param <T>
	 */
	public static <T> ApiResponse<T> error(String errorCode, String message) {
		return new ApiResponse<>(false, null, errorCode, message);
	}

	/**
	 * 실패 응답 (데이터 포함)
	 * @param errorCode
	 * @param message
	 * @param data
	 * @return
	 * @param <T>
	 */
	public static <T> ApiResponse<T> error(String errorCode, String message, T data) {
		return new ApiResponse<>(false, data, errorCode, message);
	}
}