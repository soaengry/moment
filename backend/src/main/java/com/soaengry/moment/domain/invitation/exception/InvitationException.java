package com.soaengry.moment.domain.invitation.exception;

import com.soaengry.moment.global.exception.CustomException;
import lombok.Getter;

@Getter
public class InvitationException extends CustomException {
    private final InvitationErrorCode errorCode;

    public InvitationException(InvitationErrorCode errorCode) {
        super(errorCode.name(), errorCode.getMessage(), errorCode.getHttpStatus());
        this.errorCode = errorCode;
    }

    public InvitationException(InvitationErrorCode errorCode, String customMessage) {
        super(errorCode.name(), customMessage, errorCode.getHttpStatus());
        this.errorCode = errorCode;
    }
}
