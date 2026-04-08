package com.soaengry.moment.domain.invitation.dto.response;

import com.soaengry.moment.domain.invitation.entity.Rsvp;

import java.time.LocalDateTime;

public record RsvpResponse(
        Long id,
        Long weddingId,
        String sessionId,
        Long userId,
        String attendance,
        String name,
        String side,
        String phone,
        Integer attendeeCount,
        Boolean willEat,
        Integer mealCount,
        Boolean willRide,
        Integer rideCount,
        String note,
        Boolean consent,
        LocalDateTime createdAt
) {
    public static RsvpResponse from(Rsvp rsvp) {
        return new RsvpResponse(
                rsvp.getId(),
                rsvp.getWedding().getId(),
                rsvp.getSessionId(),
                rsvp.getUser() != null ? rsvp.getUser().getId() : null,
                rsvp.getAttendance(),
                rsvp.getName(),
                rsvp.getSide(),
                rsvp.getPhone(),
                rsvp.getAttendeeCount(),
                rsvp.getWillEat(),
                rsvp.getMealCount(),
                rsvp.getWillRide(),
                rsvp.getRideCount(),
                rsvp.getNote(),
                rsvp.getConsent(),
                rsvp.getCreatedAt()
        );
    }
}
