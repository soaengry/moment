package com.soaengry.moment.domain.wedding.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WeddingErrorCode {
    // Wedding
    WEDDING_NOT_FOUND("결혼식 정보를 찾을 수 없습니다"),

    // Couple
    COUPLE_NOT_FOUND("신랑신부 정보를 찾을 수 없습니다"),

    // Schedule
    SCHEDULE_NOT_FOUND("식순 정보를 찾을 수 없습니다"),

    // AccountGroup
    ACCOUNT_GROUP_NOT_FOUND("계좌 그룹을 찾을 수 없습니다"),
    ACCOUNT_GROUP_LIMIT_EXCEEDED("계좌 그룹은 최대 3개까지 생성할 수 있습니다"),

    // Account
    ACCOUNT_NOT_FOUND("계좌 정보를 찾을 수 없습니다"),
    ACCOUNT_LIMIT_EXCEEDED("계좌 그룹당 계좌는 최대 2개까지 등록할 수 있습니다"),

    // Gallery
    GALLERY_NOT_FOUND("갤러리 이미지를 찾을 수 없습니다"),

    // Transportation
    TRANSPORTATION_NOT_FOUND("교통편 정보를 찾을 수 없습니다"),

    // Accommodation
    ACCOMMODATION_NOT_FOUND("숙박 정보를 찾을 수 없습니다"),

    // Announcement
    ANNOUNCEMENT_NOT_FOUND("공지사항을 찾을 수 없습니다"),

    // Geocoding
    GEOCODING_FAILED("주소를 좌표로 변환할 수 없습니다. 올바른 주소를 입력해주세요");

    private final String message;
}