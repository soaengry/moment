package com.soaengry.moment.domain.invitation.dto.response;

import com.soaengry.moment.domain.invitation.entity.Schedule;

import java.time.LocalTime;

public record ScheduleResponse(
        Long id,
        Long invitationId,
        LocalTime time,
        String title,
        String description,
        Integer orderIndex
) {
    public static ScheduleResponse from(Schedule schedule) {
        return new ScheduleResponse(
                schedule.getId(),
                schedule.getInvitationId(),
                schedule.getTime(),
                schedule.getTitle(),
                schedule.getDescription(),
                schedule.getOrderIndex()
        );
    }
}
