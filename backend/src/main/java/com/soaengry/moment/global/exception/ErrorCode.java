package com.soaengry.moment.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 파일 관련
    FILE_UPLOAD_FAILED("파일 업로드에 실패했습니다"),
    FILE_EMPTY("파일이 비어있습니다"),
    FILE_SIZE_EXCEEDED("파일 크기는 10MB를 초과할 수 없습니다"),
    FILE_UNSUPPORTED_FORMAT("지원하지 않는 파일 형식입니다");

    private final String message;
}
