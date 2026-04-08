package com.soaengry.moment.domain.attendance.dto.response;

import com.soaengry.moment.domain.attendance.entity.Attendance;
import com.soaengry.moment.domain.event.entity.Event;
import com.soaengry.moment.domain.wedding.entity.Host;

import java.time.LocalDateTime;
import java.util.List;

public record AttendanceResponse(
        Long id,
        Long eventId,
        String slug,
        String title,
        String date,
        String locationName,
        String locationAddress,
        String groomName,
        String brideName,
        String groomProfileImageUrl,
        String brideProfileImageUrl,
        LocalDateTime createdAt
) {

    public static AttendanceResponse from(Attendance attendance, Event event, List<Host> hosts) {
        String groomName = null;
        String brideName = null;
        String groomProfileImageUrl = null;
        String brideProfileImageUrl = null;

        for (Host host : hosts) {
            if (host.getRole() == Host.HostRole.GROOM || host.getRole() == Host.HostRole.HOST) {
                groomName = host.getName();
                groomProfileImageUrl = host.getProfileImageUrl();
            } else if (host.getRole() == Host.HostRole.BRIDE) {
                brideName = host.getName();
                brideProfileImageUrl = host.getProfileImageUrl();
            }
        }

        return new AttendanceResponse(
                attendance.getId(),
                event.getId(),
                event.getSlug(),
                event.getTitle(),
                event.getDate() != null ? event.getDate().toString() : null,
                event.getLocationName(),
                event.getLocationAddress(),
                groomName,
                brideName,
                groomProfileImageUrl,
                brideProfileImageUrl,
                attendance.getCreatedAt()
        );
    }
}
