package com.soaengry.moment.domain.email.exception;

import lombok.Getter;

@Getter
public class EmailException extends RuntimeException {
    private final EmailErrorCode errorCode;

    public EmailException(EmailErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public EmailException(EmailErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
    }
}
