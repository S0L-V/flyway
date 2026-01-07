package com.flyway.template.exception;

import lombok.Getter;

/**
 * 비즈니스 로직 예외
 *
 * 사용 예시:
 * throw new BusinessException(ErrorCode.USER_NOT_FOUND);
 * throw new BusinessException(ErrorCode.FLIGHT_FULL, "좌석이 부족합니다.");
 */
@Getter
public class BusinessException extends RuntimeException {

	private final ErrorCode errorCode;

	/**
	 * ErrorCode 만으로 예외 생성
	 * @param errorCode
	 */
	public BusinessException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}

	/**
	 * ErrorCode + 커스텀 메시지로 예외 생성
	 * @param errorCode
	 * @param message
	 */
	public BusinessException(ErrorCode errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}
}
