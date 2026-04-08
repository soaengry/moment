package com.soaengry.moment.domain.guestbook.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GuestbookErrorCode {
    GUESTBOOK_ENTRY_NOT_FOUND("방명록 항목을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    UNAUTHORIZED_ACCESS("접근 권한이 없습니다", HttpStatus.FORBIDDEN),
    INVALID_PASSWORD("비밀번호가 일치하지 않습니다", HttpStatus.FORBIDDEN);

    private final String message;
    private final HttpStatus httpStatus;
}
