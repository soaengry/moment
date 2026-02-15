package com.soaengry.moment.wedding.dto.request;

import com.soaengry.moment.wedding.entity.Schedule;

import java.time.LocalTime;

public record ScheduleRequest(
        LocalTime time,
        String title,
        String description,
        Integer orderIndex
) {
    public Schedule toEntity(Long weddingId) {
        return Schedule.create(
                weddingId,
                time,
                title,
                description,
                orderIndex
        );
    }
}