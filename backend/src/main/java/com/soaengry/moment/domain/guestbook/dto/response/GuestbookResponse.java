package com.soaengry.moment.domain.guestbook.dto.response;

import com.soaengry.moment.domain.guestbook.entity.GuestbookEntry;

import java.time.LocalDateTime;

public record GuestbookResponse(
        Long id,
        Long weddingId,
        Long userId,
        String authorName,
        String content,
        Boolean isSecret,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static GuestbookResponse from(GuestbookEntry entry) {
        return new GuestbookResponse(
                entry.getId(),
                entry.getInvitation().getId(),
                entry.getUser() != null ? entry.getUser().getId() : null,
                entry.getAuthorName(),
                entry.getContent(),
                entry.getIsSecret(),
                entry.getCreatedAt(),
                entry.getUpdatedAt()
        );
    }

    public static GuestbookResponse secretFrom(GuestbookEntry entry) {
        return new GuestbookResponse(
                entry.getId(),
                entry.getInvitation().getId(),
                entry.getUser() != null ? entry.getUser().getId() : null,
                entry.getAuthorName(),
                "비밀 메시지입니다",
                true,
                entry.getCreatedAt(),
                entry.getUpdatedAt()
        );
    }
}
