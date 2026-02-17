package com.soaengry.moment.domain.email.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EmailErrorCode {
    // 이메일 관련
    EMAIL_SEND_FAILED("이메일 발송에 실패했습니다");

    private final String message;
}
