package com.soaengry.moment.domain.rsvp.dto.response;

import com.soaengry.moment.domain.rsvp.entity.Rsvp;
import com.soaengry.moment.domain.rsvp.entity.RsvpAttendance;
import com.soaengry.moment.domain.rsvp.entity.RsvpSide;

import java.time.LocalDateTime;

public record RsvpResponse(
        Long id,
        Long weddingId,
        String sessionId,
        RsvpAttendance attendance,
        String name,
        RsvpSide side,
        String phone,
        int attendeeCount,
        MealResponse meal,
        ShuttleResponse shuttle,
        String note,
        boolean consent,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public record MealResponse(boolean willEat, int mealCount) {}
    public record ShuttleResponse(boolean willRide, int rideCount) {}

    public static RsvpResponse from(Rsvp rsvp) {
        return new RsvpResponse(
                rsvp.getId(),
                rsvp.getWedding().getId(),
                rsvp.getSessionId(),
                rsvp.getAttendance(),
                rsvp.getName(),
                rsvp.getSide(),
                rsvp.getPhone(),
                rsvp.getAttendeeCount(),
                new MealResponse(rsvp.getWillEat(), rsvp.getMealCount()),
                new ShuttleResponse(rsvp.getWillRide(), rsvp.getRideCount()),
                rsvp.getNote(),
                rsvp.getConsent(),
                rsvp.getCreatedAt(),
                rsvp.getUpdatedAt()
        );
    }
}
