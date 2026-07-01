package com.worksafe.backend.global.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // Common
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500", "서버 오류가 발생했습니다. 관리자에게 문의해 주세요."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON_400", "잘못된 요청입니다."),
    INVALID_REQUEST_BODY(HttpStatus.BAD_REQUEST, "COMMON_400_BODY", "요청 본문 형식이 올바르지 않습니다."),
    INVALID_REQUEST_PARAMETER(HttpStatus.BAD_REQUEST, "COMMON_400_PARAMETER", "요청 파라미터가 올바르지 않습니다."),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "COMMON_400_VALIDATION", "요청 값 검증에 실패했습니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_404", "요청한 리소스를 찾을 수 없습니다."),
    REQUEST_PATH_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_404_PATH", "요청한 API 경로를 찾을 수 없습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON_405", "지원하지 않는 HTTP 메서드입니다."),
    RESOURCE_CONFLICT(HttpStatus.CONFLICT, "COMMON_409", "이미 존재하는 데이터입니다."),
    DATA_INTEGRITY_VIOLATION(HttpStatus.CONFLICT, "COMMON_409_DATA_INTEGRITY", "데이터 제약 조건을 위반했습니다."),

    // Auth
    AUTH_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_401", "인증이 필요합니다."),
    AUTH_INVALID_CODE(HttpStatus.UNAUTHORIZED, "AUTH_401_CODE", "유효하지 않은 인증 코드입니다."),
    AUTH_INVALID_SOCIAL_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_401_SOCIAL_ACCESS", "유효하지 않은 소셜 액세스 토큰입니다."),
    AUTH_INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH_401_CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다."),
    AUTH_ACCESS_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_401_ACCESS", "Access Token이 만료되었습니다."),
    AUTH_REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_401_REFRESH", "Refresh Token이 만료되었습니다. 다시 로그인해 주세요."),
    AUTH_FORBIDDEN(HttpStatus.FORBIDDEN, "AUTH_403", "해당 API에 접근할 권한이 없습니다."),
    SIGNUP_FAILED(HttpStatus.CONFLICT, "AUTH_409_SIGNUP", "회원가입 요청을 처리할 수 없습니다."),

    // JWT
    JWT_INVALID_SECRET(HttpStatus.INTERNAL_SERVER_ERROR, "JWT_500_SECRET", "JWT 서명 키 설정이 올바르지 않습니다."),
    JWT_EMPTY_TOKEN(HttpStatus.UNAUTHORIZED, "JWT_401_EMPTY", "JWT 토큰이 비어 있습니다."),
    JWT_EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "JWT_401_EXPIRED", "JWT 토큰이 만료되었습니다."),
    JWT_INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "JWT_401_INVALID", "유효하지 않은 JWT 토큰입니다."),
    JWT_INVALID_SIGNATURE(HttpStatus.UNAUTHORIZED, "JWT_401_SIGNATURE", "JWT 서명이 유효하지 않습니다."),
    JWT_MALFORMED_TOKEN(HttpStatus.UNAUTHORIZED, "JWT_401_MALFORMED", "형식이 올바르지 않은 JWT 토큰입니다."),
    JWT_UNSUPPORTED_TOKEN(HttpStatus.UNAUTHORIZED, "JWT_401_UNSUPPORTED", "지원하지 않는 JWT 토큰입니다."),
    JWT_INVALID_SUBJECT(HttpStatus.UNAUTHORIZED, "JWT_401_SUBJECT", "JWT subject 값이 올바르지 않습니다."),
    JWT_MISSING_ROLE_CLAIM(HttpStatus.UNAUTHORIZED, "JWT_401_ROLE", "JWT role claim이 존재하지 않습니다."),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_404", "해당 사용자 정보를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}