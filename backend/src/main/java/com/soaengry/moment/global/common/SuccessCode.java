package com.soaengry.moment.global.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SuccessCode {
    OK(200, "요청이 성공했습니다."),
    CREATED(201, "새로운 리소스가 생성되었습니다.");

    private final int code;
    private final String message;
}
