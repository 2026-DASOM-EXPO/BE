package com.worksafe.backend.global.common.exception;

import com.worksafe.backend.global.common.response.ApiResponse;
import com.worksafe.backend.global.security.exception.JwtTokenException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({BusinessException.class, JwtTokenException.class})
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(RuntimeException e) {
        ErrorCode errorCode = resolveErrorCode(e);
        log.warn("BusinessException: {}", errorCode.getMessage());
        return fail(errorCode);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidRequestBody(Exception e) {
        log.warn("Invalid Request Body: {}", e.getMessage());
        return fail(ErrorCode.COMMON_400);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    public ResponseEntity<ApiResponse<Void>> handleValidationFailed(Exception e) {
        log.warn("Validation Failed: {}", e.getMessage());
        return fail(ErrorCode.VALIDATION_ERROR);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handlePathNotFound(Exception e) {
        log.warn("Path Not Found: {}", e.getMessage());
        return fail(ErrorCode.COMMON_404);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotAllowed(HttpRequestMethodNotSupportedException e) {
        log.warn("Method Not Allowed: {}", e.getMessage());
        return fail(ErrorCode.COMMON_400);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        log.warn("Data Integrity Violation: {}", e.getMessage());
        return fail(ErrorCode.COMMON_400);
    }

    @ExceptionHandler({AuthorizationDeniedException.class, AccessDeniedException.class})
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(Exception e) {
        log.warn("Access Denied: {}", e.getMessage());
        return fail(ErrorCode.AUTH_FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAllException(Exception e) {
        log.error("Internal Server Error", e);
        return fail(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ApiResponse<Void>> fail(ErrorCode errorCode) {
        return ResponseEntity.status(errorCode.getHttpStatus()).body(ApiResponse.error(errorCode));
    }

    private ErrorCode resolveErrorCode(RuntimeException exception) {
        if (exception instanceof BusinessException businessException) {
            return businessException.getErrorCode();
        }
        if (exception instanceof JwtTokenException jwtTokenException) {
            return jwtTokenException.getErrorCode();
        }
        return ErrorCode.INTERNAL_SERVER_ERROR;
    }
}
