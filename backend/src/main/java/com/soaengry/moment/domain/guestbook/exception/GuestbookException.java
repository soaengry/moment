package com.soaengry.moment.domain.guestbook.exception;

import com.soaengry.moment.global.exception.CustomException;
import lombok.Getter;

@Getter
public class GuestbookException extends CustomException {
    private final GuestbookErrorCode errorCode;

    public GuestbookException(GuestbookErrorCode errorCode) {
        super(errorCode.name(), errorCode.getMessage(), errorCode.getHttpStatus());
        this.errorCode = errorCode;
    }

    public GuestbookException(GuestbookErrorCode errorCode, String customMessage) {
        super(errorCode.name(), customMessage, errorCode.getHttpStatus());
        this.errorCode = errorCode;
    }
}
