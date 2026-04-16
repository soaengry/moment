package com.soaengry.moment.domain.event.dto.response;

import com.soaengry.moment.domain.event.entity.Transportation;

public record TransportationResponse(
        Long id,
        Long eventId,
        Transportation.TransportType type,
        String title,
        String description,
        Integer orderIndex
) {
    public static TransportationResponse from(Transportation t) {
        return new TransportationResponse(
                t.getId(),
                t.getEventId(),
                t.getType(),
                t.getTitle(),
                t.getDescription(),
                t.getOrderIndex()
        );
    }
}
