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
    SCHEDULE_NOT_FOUND("식순 정보를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    HOST_NOT_FOUND("주최자 정보를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    ACCOUNT_GROUP_NOT_FOUND("계좌 그룹을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    ACCOUNT_GROUP_LIMIT_EXCEEDED("계좌 그룹은 최대 4개까지 생성할 수 있습니다", HttpStatus.BAD_REQUEST),
    ACCOUNT_NOT_FOUND("계좌 정보를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    ACCOUNT_LIMIT_EXCEEDED("계좌 그룹당 계좌는 최대 3개까지 등록할 수 있습니다", HttpStatus.BAD_REQUEST),
    GEOCODING_FAILED("주소를 좌표로 변환할 수 없습니다. 올바른 주소를 입력해주세요", HttpStatus.BAD_REQUEST);

    private final String message;
    private final HttpStatus httpStatus;
}
