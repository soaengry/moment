package com.soaengry.moment.domain.wedding.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum WeddingErrorCode {
    WEDDING_NOT_FOUND("결혼식 정보를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    WEDDING_UNAUTHORIZED("해당 결혼식에 대한 권한이 없습니다", HttpStatus.FORBIDDEN),
    HOST_NOT_FOUND("주최자 정보를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    ACCOUNT_GROUP_NOT_FOUND("계좌 그룹을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    ACCOUNT_GROUP_LIMIT_EXCEEDED("계좌 그룹은 최대 4개까지 생성할 수 있습니다", HttpStatus.BAD_REQUEST),
    ACCOUNT_NOT_FOUND("계좌 정보를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    ACCOUNT_LIMIT_EXCEEDED("계좌 그룹당 계좌는 최대 3개까지 등록할 수 있습니다", HttpStatus.BAD_REQUEST),
    ANNOUNCEMENT_NOT_FOUND("공지사항을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    COUPLE_NOT_FOUND("신랑, 신부 정보를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    GALLERY_NOT_FOUND("갤러리 정보를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    TRANSPORTATION_NOT_FOUND("교통편 정보를 찾을 수 없습니다", HttpStatus.NOT_FOUND);

    private final String message;
    private final HttpStatus httpStatus;
}
