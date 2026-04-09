package com.soaengry.moment.domain.event.dto.request;

import com.soaengry.moment.domain.event.entity.EventType;

import java.time.LocalDateTime;

public record EventRequest(
        String title,
        String slug,
        EventType type,
        LocalDateTime date,
        String locationName,
        String locationAddress,
        String locationDetail,
        Boolean isPublic
) {}
