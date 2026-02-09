package com.soaengry.moment.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // 인증 관련
    AUTH_001("이메일 또는 비밀번호가 올바르지 않습니다"),
    AUTH_002("토큰이 만료되었습니다"),
    AUTH_003("이메일 인증이 필요합니다"),
    AUTH_004("로그인 시도 횟수를 초과했습니다"),
    AUTH_005("탈퇴한 계정입니다"),
    AUTH_006("인증 시도 횟수를 초과했습니다"),
    AUTH_007("인증 코드가 일치하지 않습니다"),
    AUTH_008("보안 위협이 감지되었습니다"),
    AUTH_009("토큰이 유효하지 않습니다"),

    // 검증 관련
    VALIDATION_001("비밀번호는 8자 이상, 영문 대소문자, 숫자, 특수문자를 포함해야 합니다"),
    VALIDATION_002("이메일 형식이 올바르지 않습니다"),

    // 중복 관련
    DUPLICATE_001("이미 사용 중인 이메일입니다"),
    DUPLICATE_002("이미 사용 중인 닉네임입니다"),

    // 찾을 수 없음
    NOT_FOUND_001("사용자를 찾을 수 없습니다"),
    NOT_FOUND_002("토큰을 찾을 수 없습니다"),

    // 서버 오류
    SERVER_ERROR_001("서버 오류가 발생했습니다");

    private final String message;
}
