package com.soaengry.moment.domain.invitation.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum InvitationErrorCode {
    INVITATION_NOT_FOUND("초대 정보를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    INVITATION_UNAUTHORIZED("해당 초대에 대한 권한이 없습니다", HttpStatus.FORBIDDEN),
    INVITATION_ALREADY_EXISTS("이미 초대된 사용자입니다", HttpStatus.CONFLICT),
    RSVP_NOT_FOUND("RSVP 정보를 찾을 수 없습니다", HttpStatus.NOT_FOUND);

    private final String message;
    private final HttpStatus httpStatus;
}
