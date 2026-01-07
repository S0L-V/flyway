package com.flyway.template.exception;

/**
 * 에러 코드 정의
 *
 * 코드 규칙:
 * - C001~C999: 공통 에러
 * - U001~U999: 회원 관련
 * - F001~F999: 항공편 관련
 * - B001~B999: 예약 관련
 * - P001~P999: 결제 관련
 */
public enum ErrorCode {

	// ==================== 공통 에러 (C) ====================
	INVALID_INPUT_VALUE(400, "C001", "잘못된 입력값입니다."),
	INTERNAL_SERVER_ERROR(500, "C002", "서버 오류가 발생했습니다."),
	UNAUTHORIZED(401, "C003", "인증이 필요합니다."),
	FORBIDDEN(403, "C004", "권한이 없습니다."),
	RESOURCE_NOT_FOUND(404, "C005", "요청한 리소스를 찾을 수 없습니다."),
	METHOD_NOT_ALLOWED(405, "C006", "허용되지 않은 HTTP 메서드입니다.")

	// ==================== 회원 (U) ====================
	// 예시 (팀원들이 추가)
	// USER_NOT_FOUND(404, "U001", "사용자를 찾을 수 없습니다."),
	// DUPLICATE_EMAIL(400, "U002", "이미 사용 중인 이메일입니다."),
	// INVALID_PASSWORD(400, "U003", "비밀번호가 일치하지 않습니다."),


	;

	private final int status;
	private final String code;
	private final String message;

	ErrorCode(int status, String code, String message) {
		this.status = status;
		this.code = code;
		this.message = message;
	}

	// Enum에서는 Lombok의 @Getter가 작동 안 함
	public int getStatus() {
		return status;
	}

	public String getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}
}
