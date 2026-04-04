package com.soaengry.moment.domain.invitation.exception;

import lombok.Getter;

@Getter
public class InvitationException extends RuntimeException {
    private final InvitationErrorCode errorCode;

    public InvitationException(InvitationErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public InvitationException(InvitationErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
    }
}
