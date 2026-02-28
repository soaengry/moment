package com.soaengry.moment.domain.attendance.exception;

import lombok.Getter;

@Getter
public class AttendanceException extends RuntimeException {

    private final AttendanceErrorCode errorCode;

    public AttendanceException(AttendanceErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public AttendanceException(AttendanceErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
    }
}
