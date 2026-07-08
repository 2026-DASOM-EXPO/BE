package com.worksafe.backend.global.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    COMMON_400(HttpStatus.BAD_REQUEST, "COMMON_400", "잘못된 요청입니다."),
    COMMON_401(HttpStatus.UNAUTHORIZED, "COMMON_401", "인증이 필요합니다."),
    COMMON_403(HttpStatus.FORBIDDEN, "COMMON_403", "접근 권한이 없습니다."),
    COMMON_404(HttpStatus.NOT_FOUND, "COMMON_404", "리소스를 찾을 수 없습니다."),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "입력값 검증에 실패했습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500", "서버 내부 오류가 발생했습니다."),

    AUTH_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_401", "인증이 필요합니다."),
    AUTH_FORBIDDEN(HttpStatus.FORBIDDEN, "AUTH_403", "접근 권한이 없습니다."),
    AUTH_INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH_INVALID_CREDENTIALS", "로그인 ID 또는 비밀번호가 올바르지 않습니다."),
    AUTH_DUPLICATED_LOGIN_ID(HttpStatus.CONFLICT, "AUTH_DUPLICATED_LOGIN_ID", "이미 사용 중인 로그인 ID입니다."),
    AUTH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_TOKEN_EXPIRED", "토큰이 만료되었습니다."),
    AUTH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "AUTH_TOKEN_INVALID", "올바르지 않은 토큰입니다."),
    AUTH_REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH_REFRESH_TOKEN_NOT_FOUND", "저장된 리프레시 토큰을 찾을 수 없습니다."),
    AUTH_REFRESH_TOKEN_REVOKED(HttpStatus.UNAUTHORIZED, "AUTH_REFRESH_TOKEN_REVOKED", "리프레시 토큰이 폐기되었습니다."),

    JWT_INVALID_SECRET(HttpStatus.INTERNAL_SERVER_ERROR, "JWT_INVALID_SECRET", "JWT 비밀키가 올바르지 않습니다."),
    JWT_EMPTY_TOKEN(HttpStatus.UNAUTHORIZED, "JWT_EMPTY_TOKEN", "JWT 토큰이 비어 있습니다."),
    JWT_EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "JWT_EXPIRED_TOKEN", "JWT 토큰이 만료되었습니다."),
    JWT_INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "JWT_INVALID_TOKEN", "올바르지 않은 JWT 토큰입니다."),
    JWT_INVALID_SIGNATURE(HttpStatus.UNAUTHORIZED, "JWT_INVALID_SIGNATURE", "JWT 서명이 올바르지 않습니다."),
    JWT_MALFORMED_TOKEN(HttpStatus.UNAUTHORIZED, "JWT_MALFORMED_TOKEN", "형식이 잘못된 JWT 토큰입니다."),
    JWT_UNSUPPORTED_TOKEN(HttpStatus.UNAUTHORIZED, "JWT_UNSUPPORTED_TOKEN", "지원하지 않는 JWT 토큰입니다."),
    JWT_INVALID_SUBJECT(HttpStatus.UNAUTHORIZED, "JWT_INVALID_SUBJECT", "JWT subject 값이 올바르지 않습니다."),
    JWT_MISSING_ROLE_CLAIM(HttpStatus.UNAUTHORIZED, "JWT_MISSING_ROLE_CLAIM", "JWT role 클레임이 없습니다."),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다."),
    WORKER_NOT_FOUND(HttpStatus.NOT_FOUND, "WORKER_NOT_FOUND", "작업자를 찾을 수 없습니다."),
    EQUIPMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "EQUIPMENT_NOT_FOUND", "장비를 찾을 수 없습니다."),
    SENSOR_LOG_NOT_FOUND(HttpStatus.NOT_FOUND, "SENSOR_LOG_NOT_FOUND", "센서 로그를 찾을 수 없습니다."),
    RISK_EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "RISK_EVENT_NOT_FOUND", "위험 이벤트를 찾을 수 없습니다."),
    ALERT_NOT_FOUND(HttpStatus.NOT_FOUND, "ALERT_NOT_FOUND", "알림을 찾을 수 없습니다."),
    DRONE_NOT_FOUND(HttpStatus.NOT_FOUND, "DRONE_NOT_FOUND", "드론을 찾을 수 없습니다."),
    DRONE_DISPATCH_NOT_FOUND(HttpStatus.NOT_FOUND, "DRONE_DISPATCH_NOT_FOUND", "드론 배차 정보를 찾을 수 없습니다."),
    WEARABLE_COMMAND_NOT_FOUND(HttpStatus.NOT_FOUND, "WEARABLE_COMMAND_NOT_FOUND", "웨어러블 명령을 찾을 수 없습니다."),
    INVALID_RISK_LEVEL(HttpStatus.BAD_REQUEST, "INVALID_RISK_LEVEL", "올바르지 않은 위험 단계입니다."),
    INVALID_EQUIPMENT_STATUS(HttpStatus.BAD_REQUEST, "INVALID_EQUIPMENT_STATUS", "올바르지 않은 장비 상태입니다."),
    INVALID_DRONE_STATUS(HttpStatus.BAD_REQUEST, "INVALID_DRONE_STATUS", "올바르지 않은 드론 상태입니다."),
    INVALID_DRONE_DISPATCH_STATUS(HttpStatus.BAD_REQUEST, "INVALID_DRONE_DISPATCH_STATUS", "올바르지 않은 드론 배차 상태입니다."),
    INVALID_EQUIPMENT_WEAR_STATUS(HttpStatus.BAD_REQUEST, "INVALID_EQUIPMENT_WEAR_STATUS", "올바르지 않은 착용 상태입니다."),

    ALREADY_CHECKED_IN(HttpStatus.CONFLICT, "ALREADY_CHECKED_IN", "이미 출근 처리된 작업자입니다."),
    ALREADY_CHECKED_OUT(HttpStatus.CONFLICT, "ALREADY_CHECKED_OUT", "이미 퇴근 처리된 작업자입니다."),
    DEVICE_WORKER_MISMATCH(HttpStatus.BAD_REQUEST, "DEVICE_WORKER_MISMATCH", "작업자와 장비가 일치하지 않습니다."),
    INVALID_STATE_TRANSITION(HttpStatus.BAD_REQUEST, "INVALID_STATE_TRANSITION", "상태 전이가 올바르지 않습니다."),
    DUPLICATE_SOS_REQUEST(HttpStatus.CONFLICT, "DUPLICATE_SOS_REQUEST", "중복된 SOS 요청입니다."),
    NO_AVAILABLE_DRONE(HttpStatus.CONFLICT, "NO_AVAILABLE_DRONE", "사용 가능한 드론이 없습니다."),
    DRONE_ALREADY_DISPATCHED(HttpStatus.CONFLICT, "DRONE_ALREADY_DISPATCHED", "이미 배차된 드론입니다."),
    DRONE_DROP_LOG_NOT_FOUND(HttpStatus.NOT_FOUND, "DRONE_DROP_LOG_NOT_FOUND", "드론 투하 로그를 찾을 수 없습니다."),
    INVALID_DROP_STATUS(HttpStatus.BAD_REQUEST, "INVALID_DROP_STATUS", "올바르지 않은 투하 상태입니다."),
    INVALID_COMMAND_STATUS(HttpStatus.BAD_REQUEST, "INVALID_COMMAND_STATUS", "올바르지 않은 명령 상태입니다."),
    EQUIPMENT_ALREADY_MANUAL_OVERRIDE(HttpStatus.CONFLICT, "EQUIPMENT_ALREADY_MANUAL_OVERRIDE", "이미 수동 제어 중인 장비입니다."),
    DRONE_OBSTACLE_DETECTED(HttpStatus.CONFLICT, "DRONE_OBSTACLE_DETECTED", "드론 장애물이 감지되었습니다."),
    INVALID_COORDINATE(HttpStatus.BAD_REQUEST, "INVALID_COORDINATE", "올바르지 않은 좌표입니다."),
    MISSING_REQUIRED_FIELD(HttpStatus.BAD_REQUEST, "MISSING_REQUIRED_FIELD", "필수 입력값이 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
