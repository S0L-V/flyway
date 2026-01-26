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
	METHOD_NOT_ALLOWED(405, "C006", "허용되지 않은 HTTP 메서드입니다."),

	// ==================== 회원 (U) ====================
	USER_INVALID_INPUT(400, "U001", "입력값이 올바르지 않습니다."),
	USER_EMAIL_ALREADY_EXISTS(409, "U002", "이미 가입된 이메일입니다."),
	USER_OAUTH_ALREADY_LINKED(409, "U003", "이미 연결된 OAuth 계정입니다."),
	USER_EMAIL_REQUIRED(400, "U004", "이메일 정보가 필요합니다."),
	USER_BLOCKED(403, "U005", "차단된 계정입니다."),
	USER_PASSWORD_ENCODE_ERROR(500, "U006", "비밀번호 처리 중 오류가 발생했습니다."),
	USER_DB_ERROR(500, "U007", "회원 정보 저장 중 오류가 발생했습니다."),
	USER_INVALID_SIGN_UP_ATTEMPT(400, "U008", "유효하지 않은 회원가입 요청입니다."),
	USER_ALREADY_WITHDRAWN(409, "U009", "이미 탈퇴 처리된 사용자입니다."),
	USER_NOT_WITHDRAWN(409, "U010","탈퇴 처리된 사용자만 익명화할 수 있습니다."),
	USER_INTERNAL_ERROR(500, "U999", "회원 처리 중 알 수 없는 오류가 발생했습니다."),

	// ==================== 인증 (AU) ====================
	AUTH_REFRESH_TOKEN_MISSING(401, "AU001", "리프레시 토큰이 존재하지 않습니다."),
	AUTH_REFRESH_TOKEN_INVALID(401, "AU002", "유효하지 않은 리프레시 토큰입니다."),
	AUTH_REFRESH_TOKEN_EXPIRED(401, "AU003", "만료되었거나 폐기된 리프레시 토큰입니다."),
	AUTH_REFRESH_TOKEN_REUSED(401, "AU004", "리프레시 토큰이 재사용되었습니다. 다시 로그인해 주세요."),
	AUTH_REFRESH_TOKEN_ALREADY_USED(401, "AU005", "이미 사용된 리프레시 토큰입니다."),
	AUTH_ACCESS_TOKEN_INVALID(401, "AU006", "유효하지 않은 액세스 토큰입니다."),
	AUTH_ACCESS_TOKEN_EXPIRED(401, "AU007", "만료된 액세스 토큰입니다."),
	AUTH_UNAUTHORIZED(401, "AU008", "인증이 필요합니다."),
	AUTH_FORBIDDEN(403, "AU009", "접근 권한이 없습니다."),
	AUTH_INTERNAL_ERROR(500, "AU999", "인증 처리 중 알 수 없는 오류가 발생했습니다."),

	// ==================== 관리자 (A) ====================
	ADMIN_NOT_FOUND(404, "A001", "관리자를 찾을 수 없습니다."),
	ADMIN_LOGIN_FAILED(401, "A002", "이메일 또는 비밀번호가 일치하지 않습니다."),
	ADMIN_ACCOUNT_LOCKED(403, "A003", "계정이 잠겼습니다. 잠시 후 다시 시도해주세요."),
	ADMIN_ACCOUNT_INACTIVE(403, "A004", "비활성화된 계정입니다."),
	ADMIN_INVALID_TOKEN(401, "A005", "유효하지 않은 토큰입니다."),
	ADMIN_TOKEN_EXPIRED(401, "A006", "만료된 토큰입니다."),
	ADMIN_INSUFFICIENT_PERMISSION(403, "A007", "권한이 없습니다."),
	ADMIN_DUPLICATE_EMAIL(400, "A008", "이미 사용 중인 이메일입니다.")
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
