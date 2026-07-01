package com.worksafe.backend.global.security.exception;

import com.worksafe.backend.global.common.exception.BusinessException;
import com.worksafe.backend.global.common.exception.ErrorCode;

public class JwtTokenException extends BusinessException {

    public JwtTokenException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public JwtTokenException(ErrorCode errorCode) {
        super(errorCode);
    }
}
