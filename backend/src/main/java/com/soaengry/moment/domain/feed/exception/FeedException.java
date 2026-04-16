package com.soaengry.moment.domain.feed.exception;

import com.soaengry.moment.global.exception.CustomException;
import lombok.Getter;

@Getter
public class FeedException extends CustomException {
    private final FeedErrorCode errorCode;

    public FeedException(FeedErrorCode errorCode) {
        super(errorCode.name(), errorCode.getMessage(), errorCode.getHttpStatus());
        this.errorCode = errorCode;
    }

    public FeedException(FeedErrorCode errorCode, String customMessage) {
        super(errorCode.name(), customMessage, errorCode.getHttpStatus());
        this.errorCode = errorCode;
    }
}
