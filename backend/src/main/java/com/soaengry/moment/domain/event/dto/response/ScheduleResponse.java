package com.soaengry.moment.domain.event.dto.response;

import com.soaengry.moment.domain.event.entity.Schedule;

public record ScheduleResponse(
        Long id,
        Long eventId,
        String title,
        String description,
        Integer orderIndex
) {
    public static ScheduleResponse from(Schedule schedule) {
        return new ScheduleResponse(
                schedule.getId(),
                schedule.getEventId(),
                schedule.getTitle(),
                schedule.getDescription(),
                schedule.getOrderIndex()
        );
    }
}
