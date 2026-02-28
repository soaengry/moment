package com.soaengry.moment.domain.attendance.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AttendanceErrorCode {

    ATTENDANCE_NOT_FOUND("일정을 찾을 수 없습니다"),
    DUPLICATE_ATTENDANCE("이미 등록된 일정입니다");

    private final String message;
}
