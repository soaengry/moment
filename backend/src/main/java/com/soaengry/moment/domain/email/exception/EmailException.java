package com.soaengry.moment.domain.email.exception;

import com.soaengry.moment.global.exception.CustomException;
import lombok.Getter;

@Getter
public class EmailException extends CustomException {
    private final EmailErrorCode errorCode;

    public EmailException(EmailErrorCode errorCode) {
        super(errorCode.name(), errorCode.getMessage(), errorCode.getHttpStatus());
        this.errorCode = errorCode;
    }

    public EmailException(EmailErrorCode errorCode, String customMessage) {
        super(errorCode.name(), customMessage, errorCode.getHttpStatus());
        this.errorCode = errorCode;
    }
}
