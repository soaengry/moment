package com.soaengry.moment.domain.attendance.dto.response;

import com.soaengry.moment.domain.attendance.entity.Attendance;
import com.soaengry.moment.domain.invitation.entity.Couple;
import com.soaengry.moment.domain.invitation.entity.Invitation;

import java.time.LocalDateTime;
import java.util.List;

public record AttendanceResponse(
        Long id,
        Long weddingId,
        String invitationId,
        String title,
        LocalDateTime eventDate,
        String venueName,
        String venueAddress,
        String groomName,
        String brideName,
        String groomProfileImageUrl,
        String brideProfileImageUrl,
        LocalDateTime createdAt
) {

    public static AttendanceResponse from(Attendance attendance, Invitation wedding, List<Couple> couples) {
        String groomName = null;
        String brideName = null;
        String groomProfileImageUrl = null;
        String brideProfileImageUrl = null;

        for (Couple couple : couples) {
            if (couple.getRole() == Couple.CoupleRole.GROOM) {
                groomName = couple.getName();
                groomProfileImageUrl = couple.getProfileImageUrl();
            } else if (couple.getRole() == Couple.CoupleRole.BRIDE) {
                brideName = couple.getName();
                brideProfileImageUrl = couple.getProfileImageUrl();
            }
        }

        return new AttendanceResponse(
                attendance.getId(),
                wedding.getId(),
                wedding.getInvitationId(),
                wedding.getTitle(),
                wedding.getEventDate(),
                wedding.getVenueName(),
                wedding.getVenueAddress(),
                groomName,
                brideName,
                groomProfileImageUrl,
                brideProfileImageUrl,
                attendance.getCreatedAt()
        );
    }
}
