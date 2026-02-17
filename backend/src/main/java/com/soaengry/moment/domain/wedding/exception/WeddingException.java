package com.soaengry.moment.domain.wedding.exception;

import lombok.Getter;

@Getter
public class WeddingException extends RuntimeException {
    private final WeddingErrorCode errorCode;

    public WeddingException(WeddingErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public WeddingException(WeddingErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
    }
}