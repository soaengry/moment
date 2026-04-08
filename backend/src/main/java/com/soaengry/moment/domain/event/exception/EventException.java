package com.soaengry.moment.domain.event.exception;

import com.soaengry.moment.global.exception.CustomException;
import lombok.Getter;

@Getter
public class EventException extends CustomException {
    private final EventErrorCode errorCode;

    public EventException(EventErrorCode errorCode) {
        super(errorCode.name(), errorCode.getMessage(), errorCode.getHttpStatus());
        this.errorCode = errorCode;
    }

    public EventException(EventErrorCode errorCode, String customMessage) {
        super(errorCode.name(), customMessage, errorCode.getHttpStatus());
        this.errorCode = errorCode;
    }
}
