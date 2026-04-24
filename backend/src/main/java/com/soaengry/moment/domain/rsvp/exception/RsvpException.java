package com.soaengry.moment.domain.rsvp.exception;

import com.soaengry.moment.global.exception.CustomException;
import lombok.Getter;

@Getter
public class RsvpException extends CustomException {
    private final RsvpErrorCode errorCode;

    public RsvpException(RsvpErrorCode errorCode) {
        super(errorCode.name(), errorCode.getMessage(), errorCode.getHttpStatus());
        this.errorCode = errorCode;
    }
}
