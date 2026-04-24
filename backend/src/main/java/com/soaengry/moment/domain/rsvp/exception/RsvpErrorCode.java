package com.soaengry.moment.domain.rsvp.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RsvpErrorCode {
    RSVP_NOT_FOUND("RSVP를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    RSVP_ALREADY_EXISTS("이미 참석 여부를 전달했습니다", HttpStatus.CONFLICT),
    RSVP_UNAUTHORIZED("수정 권한이 없습니다", HttpStatus.FORBIDDEN),
    WEDDING_NOT_FOUND("웨딩 정보를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    HOST_ONLY("호스트만 접근할 수 있습니다", HttpStatus.FORBIDDEN);

    private final String message;
    private final HttpStatus httpStatus;
}
