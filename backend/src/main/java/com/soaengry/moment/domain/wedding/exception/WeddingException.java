package com.soaengry.moment.domain.wedding.exception;

import com.soaengry.moment.global.exception.CustomException;
import lombok.Getter;

@Getter
public class WeddingException extends CustomException {
    private final WeddingErrorCode errorCode;

    public WeddingException(WeddingErrorCode errorCode) {
        super(errorCode.name(), errorCode.getMessage(), errorCode.getHttpStatus());
        this.errorCode = errorCode;
    }

    public WeddingException(WeddingErrorCode errorCode, String customMessage) {
        super(errorCode.name(), customMessage, errorCode.getHttpStatus());
        this.errorCode = errorCode;
    }
}
