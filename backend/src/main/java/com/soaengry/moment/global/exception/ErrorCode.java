package com.soaengry.moment.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public class ErrorCode {

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    // 기본 파일 관련 에러 코드
    public static final ErrorCode FILE_UPLOAD_FAILED = new ErrorCode("FILE_UPLOAD_FAILED", "파일 업로드에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR);
    public static final ErrorCode FILE_EMPTY = new ErrorCode("FILE_EMPTY", "파일이 비어있습니다", HttpStatus.BAD_REQUEST);
    public static final ErrorCode FILE_SIZE_EXCEEDED = new ErrorCode("FILE_SIZE_EXCEEDED", "파일 크기는 10MB를 초과할 수 없습니다", HttpStatus.BAD_REQUEST);
    public static final ErrorCode FILE_UNSUPPORTED_FORMAT = new ErrorCode("FILE_UNSUPPORTED_FORMAT", "지원하지 않는 파일 형식입니다", HttpStatus.BAD_REQUEST);

    /**
     * 동적 ErrorCode 생성 (도메인별 ErrorCode를 글로벌 ErrorCode로 변환)
     */
    public static ErrorCode from(String code, String message, HttpStatus httpStatus) {
        return new ErrorCode(code, message, httpStatus);
    }
}
