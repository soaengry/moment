package com.soaengry.moment.domain.feed.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FeedErrorCode {
    POST_NOT_FOUND("게시글을 찾을 수 없습니다"),
    COMMENT_NOT_FOUND("댓글을 찾을 수 없습니다"),
    UNAUTHORIZED_ACCESS("접근 권한이 없습니다"),
    IMAGE_LIMIT_EXCEEDED("이미지는 최대 4장까지 등록할 수 있습니다"),
    CONTENT_TOO_LONG("게시글은 최대 200자까지 작성할 수 있습니다");

    private final String message;
}
