package com.soaengry.moment.domain.user.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode {
    // 인증 관련
    AUTH_INVALID_CREDENTIALS("이메일 또는 비밀번호가 올바르지 않습니다"),
    AUTH_TOKEN_EXPIRED("토큰이 만료되었습니다"),
    AUTH_EMAIL_NOT_VERIFIED("이메일 인증이 필요합니다"),
    AUTH_LOGIN_ATTEMPTS_EXCEEDED("로그인 시도 횟수를 초과했습니다"),
    AUTH_ACCOUNT_WITHDRAWN("탈퇴한 계정입니다"),
    AUTH_VERIFICATION_ATTEMPTS_EXCEEDED("인증 시도 횟수를 초과했습니다"),
    AUTH_VERIFICATION_CODE_MISMATCH("인증 코드가 일치하지 않습니다"),
    AUTH_SECURITY_THREAT_DETECTED("보안 위협이 감지되었습니다"),
    AUTH_INVALID_TOKEN("토큰이 유효하지 않습니다"),

    // 검증 관련
    VALIDATION_INVALID_PASSWORD("비밀번호는 8자 이상, 영문 대소문자, 숫자, 특수문자를 포함해야 합니다"),
    VALIDATION_INVALID_EMAIL("이메일 형식이 올바르지 않습니다"),

    // 중복 관련
    DUPLICATE_EMAIL("이미 사용 중인 이메일입니다"),
    DUPLICATE_NICKNAME("이미 사용 중인 닉네임입니다"),

    // 찾을 수 없음
    USER_NOT_FOUND("사용자를 찾을 수 없습니다"),
    TOKEN_NOT_FOUND("토큰을 찾을 수 없습니다");

    private final String message;
}
