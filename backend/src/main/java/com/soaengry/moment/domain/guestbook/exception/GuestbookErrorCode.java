package com.soaengry.moment.domain.guestbook.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GuestbookErrorCode {
    GUESTBOOK_ENTRY_NOT_FOUND("방명록 항목을 찾을 수 없습니다"),
    UNAUTHORIZED_ACCESS("접근 권한이 없습니다"),
    INVALID_PASSWORD("비밀번호가 일치하지 않습니다");

    private final String message;
}
