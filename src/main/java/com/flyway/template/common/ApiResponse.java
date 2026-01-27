package com.flyway.template.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * API 공통 응답 형식
 *
 * @Param <T> 응답 데이터 확인
 */
@Getter
@NoArgsConstructor
public class ApiResponse<T> {

	private boolean success;
	private T data;
	private String errorCode;
	private String message;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private PageInfo page;

	public ApiResponse(boolean success, T data, String errorCode, String message) {
		this.success = success;
		this.data = data;
		this.errorCode = errorCode;
		this.message = message;
	}

	public ApiResponse(boolean success, T data, String errorCode, String message, PageInfo page) {
		this.success = success;
		this.data = data;
		this.errorCode = errorCode;
		this.message = message;
		this.page = page;
	}

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
	 * 성공 응답 (데이터 + 페이지 정보)
	 * @param data
	 * @param page
	 * @return
	 * @param <T>
	 */
	public static <T> ApiResponse<T> success(T data, PageInfo page) {
		return new ApiResponse<>(true, data, null, null, page);
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
