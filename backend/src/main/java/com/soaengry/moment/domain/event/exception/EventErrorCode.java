package com.soaengry.moment.domain.event.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum EventErrorCode {
    EVENT_NOT_FOUND("이벤트 정보를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    EVENT_UNAUTHORIZED("해당 이벤트에 대한 권한이 없습니다", HttpStatus.FORBIDDEN),
    EVENT_SLUG_DUPLICATED("이미 사용 중인 슬러그입니다", HttpStatus.CONFLICT),
    HERO_IMAGE_NOT_FOUND("히어로 이미지를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    TRANSPORTATION_NOT_FOUND("교통편 정보를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    ANNOUNCEMENT_NOT_FOUND("공지사항을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    GEOCODING_FAILED("주소를 좌표로 변환할 수 없습니다. 올바른 주소를 입력해주세요", HttpStatus.BAD_REQUEST);

    private final String message;
    private final HttpStatus httpStatus;
}
