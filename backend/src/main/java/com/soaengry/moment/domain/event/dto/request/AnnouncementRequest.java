package com.soaengry.moment.domain.event.dto.request;

public record AnnouncementRequest(
        String title,
        String content,
        Boolean isPinned
) {}
