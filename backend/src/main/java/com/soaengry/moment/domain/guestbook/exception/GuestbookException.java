package com.soaengry.moment.domain.guestbook.exception;

import lombok.Getter;

@Getter
public class GuestbookException extends RuntimeException {
    private final GuestbookErrorCode errorCode;

    public GuestbookException(GuestbookErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
