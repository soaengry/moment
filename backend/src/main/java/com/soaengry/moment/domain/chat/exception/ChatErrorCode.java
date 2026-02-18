package com.soaengry.moment.domain.chat.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChatErrorCode {
    CHAT_ROOM_NOT_FOUND("채팅방을 찾을 수 없습니다"),
    UNAUTHORIZED_ACCESS("접근 권한이 없습니다");

    private final String message;
}
