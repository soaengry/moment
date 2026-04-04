package com.soaengry.moment.domain.invitation.dto.request;

import com.soaengry.moment.domain.invitation.entity.Announcement;

public record AnnouncementRequest(
        String title,
        String content,
        Boolean isPinned
) {
    public Announcement toEntity(Long invitationId) {
        return Announcement.create(invitationId, title, content, isPinned);
    }
}
