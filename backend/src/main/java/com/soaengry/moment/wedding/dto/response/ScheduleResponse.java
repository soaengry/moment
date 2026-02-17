package com.soaengry.moment.wedding.dto.response;

import com.soaengry.moment.wedding.entity.Schedule;

import java.time.LocalTime;

public record ScheduleResponse(
        Long id,
        Long weddingId,
        LocalTime time,
        String title,
        String description,
        Integer orderIndex
) {
    public static ScheduleResponse from(Schedule schedule) {
        return new ScheduleResponse(
                schedule.getId(),
                schedule.getWeddingId(),
                schedule.getTime(),
                schedule.getTitle(),
                schedule.getDescription(),
                schedule.getOrderIndex()
        );
    }
}