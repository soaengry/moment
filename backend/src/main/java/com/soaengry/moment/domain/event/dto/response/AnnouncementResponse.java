package com.soaengry.moment.domain.event.dto.response;

import com.soaengry.moment.domain.event.entity.Announcement;

import java.time.LocalDateTime;

public record AnnouncementResponse(
        Long id,
        Long eventId,
        String title,
        String content,
        Boolean isPinned,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static AnnouncementResponse from(Announcement a) {
        return new AnnouncementResponse(
                a.getId(),
                a.getEventId(),
                a.getTitle(),
                a.getContent(),
                a.getIsPinned(),
                a.getCreatedAt(),
                a.getUpdatedAt()
        );
    }
}
