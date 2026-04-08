package com.soaengry.moment.domain.invitation.dto.request;

public record RsvpRequest(
        String sessionId,
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
        Boolean consent
) {}
