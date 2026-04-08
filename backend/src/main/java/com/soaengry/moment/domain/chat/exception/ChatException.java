package com.soaengry.moment.domain.chat.exception;

import com.soaengry.moment.global.exception.CustomException;
import lombok.Getter;

@Getter
public class ChatException extends CustomException {
    private final ChatErrorCode errorCode;

    public ChatException(ChatErrorCode errorCode) {
        super(errorCode.name(), errorCode.getMessage(), errorCode.getHttpStatus());
        this.errorCode = errorCode;
    }

    public ChatException(ChatErrorCode errorCode, String customMessage) {
        super(errorCode.name(), customMessage, errorCode.getHttpStatus());
        this.errorCode = errorCode;
    }
}
