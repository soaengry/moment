package com.soaengry.moment.domain.invitation.dto.response;

import com.soaengry.moment.domain.invitation.entity.Announcement;

import java.time.LocalDateTime;

public record AnnouncementResponse(
        Long id,
        Long invitationId,
        String title,
        String content,
        Boolean isPinned,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static AnnouncementResponse from(Announcement announcement) {
        return new AnnouncementResponse(
                announcement.getId(),
                announcement.getInvitationId(),
                announcement.getTitle(),
                announcement.getContent(),
                announcement.getIsPinned(),
                announcement.getCreatedAt(),
                announcement.getUpdatedAt()
        );
    }
}
