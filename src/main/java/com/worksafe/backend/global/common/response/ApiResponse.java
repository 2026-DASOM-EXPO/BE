package com.worksafe.backend.global.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.worksafe.backend.global.common.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResponse<T> {

    private final String code;
    private final String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final T data;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("200", "성공", data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>("200", message, data);
    }

    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<>("201", "생성 성공", data);
    }

    public static <T> ApiResponse<T> created(String message, T data) {
        return new ApiResponse<>("201", message, data);
    }

    public static ApiResponse<Void> success() {
        return new ApiResponse<>("200", "성공", null);
    }

    public static ApiResponse<Void> fail(String code, String message) {
        return new ApiResponse<>(code, message, null);
    }

    public static ApiResponse<Void> error(ErrorCode errorCode) { return new ApiResponse<>(errorCode.getCode(), errorCode.getMessage(), null); }
}