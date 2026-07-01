package com.worksafe.backend.global.common.exception;

public class CustomException extends BusinessException {

    public CustomException(ErrorCode errorCode) {
        super(errorCode);
    }

    public CustomException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
