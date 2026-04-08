package com.soaengry.moment.domain.attendance.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AttendanceErrorCode {

    ATTENDANCE_NOT_FOUND("일정을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    DUPLICATE_ATTENDANCE("이미 등록된 일정입니다", HttpStatus.CONFLICT);

    private final String message;
    private final HttpStatus httpStatus;
}
