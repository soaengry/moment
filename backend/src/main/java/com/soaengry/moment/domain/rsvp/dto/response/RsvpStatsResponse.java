package com.soaengry.moment.domain.rsvp.dto.response;

public record RsvpStatsResponse(
        long totalRsvpCount,
        long attendingCount,
        long totalAttendeeCount,
        long totalMealCount,
        long totalShuttleCount
) {}
