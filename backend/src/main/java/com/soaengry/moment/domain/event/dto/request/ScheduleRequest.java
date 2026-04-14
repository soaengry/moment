package com.soaengry.moment.domain.event.dto.request;

import com.soaengry.moment.domain.event.entity.Schedule;

public record ScheduleRequest(
        String title,
        String description,
        Integer orderIndex
) {
    public Schedule toEntity(Long eventId) {
        return Schedule.create(eventId, title, description, orderIndex);
    }
}
