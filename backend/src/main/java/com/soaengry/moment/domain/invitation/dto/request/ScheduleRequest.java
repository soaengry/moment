package com.soaengry.moment.domain.invitation.dto.request;

import com.soaengry.moment.domain.invitation.entity.Schedule;

import java.time.LocalTime;

public record ScheduleRequest(
        LocalTime time,
        String title,
        String description,
        Integer orderIndex
) {
    public Schedule toEntity(Long invitationId) {
        return Schedule.create(invitationId, time, title, description, orderIndex);
    }
}
