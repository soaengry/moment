package com.soaengry.moment.domain.wedding.dto.request;

import com.soaengry.moment.domain.wedding.entity.Schedule;

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