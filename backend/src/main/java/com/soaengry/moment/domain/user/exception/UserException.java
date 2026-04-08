package com.soaengry.moment.domain.user.exception;

import com.soaengry.moment.global.exception.CustomException;
import lombok.Getter;

@Getter
public class UserException extends CustomException {
    private final UserErrorCode errorCode;

    public UserException(UserErrorCode errorCode) {
        super(errorCode.name(), errorCode.getMessage(), errorCode.getHttpStatus());
        this.errorCode = errorCode;
    }

    public UserException(UserErrorCode errorCode, String customMessage) {
        super(errorCode.name(), customMessage, errorCode.getHttpStatus());
        this.errorCode = errorCode;
    }
}
