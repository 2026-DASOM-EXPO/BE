package com.worksafe.backend.global.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    COMMON_400(HttpStatus.BAD_REQUEST, "COMMON_400", "Bad request."),
    COMMON_401(HttpStatus.UNAUTHORIZED, "COMMON_401", "Authentication required."),
    COMMON_403(HttpStatus.FORBIDDEN, "COMMON_403", "Access denied."),
    COMMON_404(HttpStatus.NOT_FOUND, "COMMON_404", "Resource not found."),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Validation failed."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500", "Internal server error."),

    AUTH_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_401", "Authentication required."),
    AUTH_FORBIDDEN(HttpStatus.FORBIDDEN, "AUTH_403", "Access denied."),
    AUTH_INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH_INVALID_CREDENTIALS", "Invalid username or password."),
    AUTH_DUPLICATED_USERNAME(HttpStatus.CONFLICT, "AUTH_DUPLICATED_USERNAME", "Username already exists."),
    AUTH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_TOKEN_EXPIRED", "Token expired."),
    AUTH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "AUTH_TOKEN_INVALID", "Invalid token."),
    AUTH_REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH_REFRESH_TOKEN_NOT_FOUND", "Refresh token not found."),
    AUTH_REFRESH_TOKEN_REVOKED(HttpStatus.UNAUTHORIZED, "AUTH_REFRESH_TOKEN_REVOKED", "Refresh token revoked."),

    JWT_INVALID_SECRET(HttpStatus.INTERNAL_SERVER_ERROR, "JWT_INVALID_SECRET", "Invalid JWT secret."),
    JWT_EMPTY_TOKEN(HttpStatus.UNAUTHORIZED, "JWT_EMPTY_TOKEN", "JWT token is empty."),
    JWT_EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "JWT_EXPIRED_TOKEN", "JWT token expired."),
    JWT_INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "JWT_INVALID_TOKEN", "Invalid JWT token."),
    JWT_INVALID_SIGNATURE(HttpStatus.UNAUTHORIZED, "JWT_INVALID_SIGNATURE", "Invalid JWT signature."),
    JWT_MALFORMED_TOKEN(HttpStatus.UNAUTHORIZED, "JWT_MALFORMED_TOKEN", "Malformed JWT token."),
    JWT_UNSUPPORTED_TOKEN(HttpStatus.UNAUTHORIZED, "JWT_UNSUPPORTED_TOKEN", "Unsupported JWT token."),
    JWT_INVALID_SUBJECT(HttpStatus.UNAUTHORIZED, "JWT_INVALID_SUBJECT", "Invalid JWT subject."),
    JWT_MISSING_ROLE_CLAIM(HttpStatus.UNAUTHORIZED, "JWT_MISSING_ROLE_CLAIM", "Missing JWT role claim."),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User not found."),
    WORKER_NOT_FOUND(HttpStatus.NOT_FOUND, "WORKER_NOT_FOUND", "Worker not found."),
    EQUIPMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "EQUIPMENT_NOT_FOUND", "Equipment not found."),
    SENSOR_LOG_NOT_FOUND(HttpStatus.NOT_FOUND, "SENSOR_LOG_NOT_FOUND", "Sensor log not found."),
    RISK_EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "RISK_EVENT_NOT_FOUND", "Risk event not found."),
    ALERT_NOT_FOUND(HttpStatus.NOT_FOUND, "ALERT_NOT_FOUND", "Alert not found."),
    DRONE_NOT_FOUND(HttpStatus.NOT_FOUND, "DRONE_NOT_FOUND", "Drone not found."),
    DRONE_DISPATCH_NOT_FOUND(HttpStatus.NOT_FOUND, "DRONE_DISPATCH_NOT_FOUND", "Drone dispatch not found."),
    WEARABLE_COMMAND_NOT_FOUND(HttpStatus.NOT_FOUND, "WEARABLE_COMMAND_NOT_FOUND", "Wearable command not found."),
    INVALID_RISK_LEVEL(HttpStatus.BAD_REQUEST, "INVALID_RISK_LEVEL", "Invalid risk level."),
    INVALID_EQUIPMENT_STATUS(HttpStatus.BAD_REQUEST, "INVALID_EQUIPMENT_STATUS", "Invalid equipment status."),
    INVALID_DRONE_STATUS(HttpStatus.BAD_REQUEST, "INVALID_DRONE_STATUS", "Invalid drone status."),
    INVALID_DRONE_DISPATCH_STATUS(HttpStatus.BAD_REQUEST, "INVALID_DRONE_DISPATCH_STATUS", "Invalid drone dispatch status."),
    INVALID_EQUIPMENT_WEAR_STATUS(HttpStatus.BAD_REQUEST, "INVALID_EQUIPMENT_WEAR_STATUS", "Invalid wear status."),

    ALREADY_CHECKED_IN(HttpStatus.CONFLICT, "ALREADY_CHECKED_IN", "Worker is already checked in."),
    ALREADY_CHECKED_OUT(HttpStatus.CONFLICT, "ALREADY_CHECKED_OUT", "Worker is already checked out."),
    DEVICE_WORKER_MISMATCH(HttpStatus.BAD_REQUEST, "DEVICE_WORKER_MISMATCH", "Worker and equipment do not match."),
    INVALID_STATE_TRANSITION(HttpStatus.BAD_REQUEST, "INVALID_STATE_TRANSITION", "Invalid state transition."),
    DUPLICATE_SOS_REQUEST(HttpStatus.CONFLICT, "DUPLICATE_SOS_REQUEST", "Duplicate SOS request."),
    NO_AVAILABLE_DRONE(HttpStatus.CONFLICT, "NO_AVAILABLE_DRONE", "No available drone."),
    DRONE_ALREADY_DISPATCHED(HttpStatus.CONFLICT, "DRONE_ALREADY_DISPATCHED", "Drone already dispatched."),
    DRONE_DROP_LOG_NOT_FOUND(HttpStatus.NOT_FOUND, "DRONE_DROP_LOG_NOT_FOUND", "Drone drop log not found."),
    INVALID_DROP_STATUS(HttpStatus.BAD_REQUEST, "INVALID_DROP_STATUS", "Invalid drop status."),
    INVALID_COMMAND_STATUS(HttpStatus.BAD_REQUEST, "INVALID_COMMAND_STATUS", "Invalid command status."),
    EQUIPMENT_ALREADY_MANUAL_OVERRIDE(HttpStatus.CONFLICT, "EQUIPMENT_ALREADY_MANUAL_OVERRIDE", "Equipment already manual override."),
    DRONE_OBSTACLE_DETECTED(HttpStatus.CONFLICT, "DRONE_OBSTACLE_DETECTED", "Drone obstacle detected."),
    INVALID_COORDINATE(HttpStatus.BAD_REQUEST, "INVALID_COORDINATE", "Invalid coordinate."),
    MISSING_REQUIRED_FIELD(HttpStatus.BAD_REQUEST, "MISSING_REQUIRED_FIELD", "Missing required field.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
