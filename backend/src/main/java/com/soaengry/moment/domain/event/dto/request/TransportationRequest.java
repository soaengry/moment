package com.soaengry.moment.domain.event.dto.request;

import com.soaengry.moment.domain.event.entity.Transportation;

public record TransportationRequest(
        Transportation.TransportType type,
        String title,
        String description,
        Integer orderIndex
) {
    public Transportation toEntity(Long eventId) {
        return Transportation.create(eventId, type, title, description, orderIndex);
    }
}
