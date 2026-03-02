package com.soaengry.moment.domain.chat.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChatErrorCode {
    CHAT_WEDDING_NOT_FOUND("웨딩을 찾을 수 없습니다"),
    CHAT_IMAGE_UPLOAD_FAILED("채팅 이미지 업로드에 실패했습니다"),
    UNAUTHORIZED_ACCESS("접근 권한이 없습니다");

    private final String message;
}
