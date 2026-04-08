package com.soaengry.moment.domain.attendance.exception;

import com.soaengry.moment.global.exception.CustomException;
import lombok.Getter;

@Getter
public class AttendanceException extends CustomException {
    private final AttendanceErrorCode errorCode;

    public AttendanceException(AttendanceErrorCode errorCode) {
        super(errorCode.name(), errorCode.getMessage(), errorCode.getHttpStatus());
        this.errorCode = errorCode;
    }

    public AttendanceException(AttendanceErrorCode errorCode, String customMessage) {
        super(errorCode.name(), customMessage, errorCode.getHttpStatus());
        this.errorCode = errorCode;
    }
}
