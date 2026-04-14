package com.soaengry.moment.domain.chat.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ChatErrorCode {
    CHAT_WEDDING_NOT_FOUND("일정을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    CHAT_IMAGE_UPLOAD_FAILED("채팅 이미지 업로드에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    UNAUTHORIZED_ACCESS("접근 권한이 없습니다", HttpStatus.FORBIDDEN);

    private final String message;
    private final HttpStatus httpStatus;
}
