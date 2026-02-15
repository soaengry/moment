package com.soaengry.moment.wedding.dto.request;

import com.soaengry.moment.wedding.entity.Announcement;

public record AnnouncementRequest(
        String title,
        String content,
        Boolean isPinned
) {
    public Announcement toEntity(Long weddingId) {
        return Announcement.create(
                weddingId,
                title,
                content,
                isPinned
        );
    }
}