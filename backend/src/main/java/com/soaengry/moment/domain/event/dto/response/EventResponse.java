package com.soaengry.moment.domain.event.dto.response;

import com.soaengry.moment.domain.event.entity.Event;
import com.soaengry.moment.domain.event.entity.EventType;
import com.soaengry.moment.domain.event.entity.RecurrenceType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record EventResponse(
        Long id,
        Long userId,
        String title,
        EventType type,
        LocalDateTime date,
        String locationName,
        String locationAddress,
        String locationDetail,
        Double locationLat,
        Double locationLng,
        String slug,
        boolean isPublic,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        RecurrenceType recurrenceType,
        String recurrenceDays,
        LocalDate recurrenceEndDate
) {
    public static EventResponse from(Event event) {
        return new EventResponse(
                event.getId(),
                event.getUser().getId(),
                event.getTitle(),
                event.getType(),
                event.getDate(),
                event.getLocationName(),
                event.getLocationAddress(),
                event.getLocationDetail(),
                event.getLocationLat(),
                event.getLocationLng(),
                event.getSlug(),
                event.isPublic(),
                event.getCreatedAt(),
                event.getUpdatedAt(),
                event.getRecurrenceType(),
                event.getRecurrenceDays(),
                event.getRecurrenceEndDate()
        );
    }
}
